/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import jakarta.servlet.ServletContextListener;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.slf4j.Logger;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.codeaffine.tiny.shared.IoUtils.findFreePort;
import static com.codeaffine.tiny.shared.Metric.measureDuration;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static com.codeaffine.tiny.star.Texts.*;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.max;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <p>An {@link ApplicationServer} is an embedded Servlet engine adapter used to launch
 * standalone <a href="https://eclipse.dev/rap/developers-guide/rwt-standalone.html" target="_blank">RWT applications</a>.
 * It manages the lifecycle of an RWT application (start and stop) defined by an
 * <a href="https://eclipse.dev/rap/developers-guide/application-configuration.html">{@link ApplicationConfiguration}</a>.</p>
 *
 * <p>The simplest way to start an application is as follows:</p>
 * <pre><code class="language-java">
 * public class DemoApplication {
 *     public static void main(String[] args) {
 *         ApplicationServerBuilder
 *             .newApplicationServerBuilder(new DemoApplicationConfiguration())
 *             .build()
 *             .start();
 *     }
 * }
 * </code></pre>
 *
 * <p>Launching the snippet above will start a server instance on a random port and print
 * something similar to the following output to the console:</p>
 * <pre>
 * INFO: Application server working directory: [...]
 * INFO: Starting com.codeaffine.tiny.star.applicationserver with embedded Undertow.
 * INFO: Creation of com.codeaffine.tiny.star.applicationserver application process took 106 ms.
 * INFO: Application Entrypoint URL: http://localhost:52963/ui
 * INFO: Starting com.codeaffine.tiny.star.applicationserver application process took 356 ms.
 * </pre>
 *
 * <p>An {@link ApplicationServer} instance can be configured using the {@link ApplicationServerBuilder} class. The builder
 * provides a fluent API for configuring the server's parameters, such as the host, port, working directory, and
 * session timeout. The builder allows to register lifecycle listeners, servlet context listeners,
 * and filter definitions. Most of the configuration settings can also be provided by an environment variable.
 * For more details, see the {@link ApplicationServerBuilder}'s class documentation.</p>
 *
 * <p>A notable capability of the {@link ApplicationServer} is its ability to observe lifecycle events. You can register
 * lifecycle listeners that will be notified when the server enters different states, such as
 * {@link State#STARTING}, {@link State#RUNNING}, {@link State#STOPPING}, and {@link State#HALTED}. This allows you to
 * perform custom actions or logging at various points in the server's lifecycle.</p>
 *
 * <p><strong>Advanced:</strong> The servlet engine itself is pluggable and provided as a service implementing the
 * {@link com.codeaffine.tiny.star.spi.Server} interface. To supply your own server implementation, refer to the
 * {@link com.codeaffine.tiny.star.spi.ServerFactory} and ensure compliance with contract tests from the
 * <code>com.codeaffine.tiny.star.tck</code> module.</p>
 */
@Builder(
    builderClassName = "InternalApplicationServerBuilder",
    builderMethodName = "newDefaultApplicationServerBuilder",
    setterPrefix = "with",
    access = PRIVATE
)
public class ApplicationServer {

    /**
     * The environment variable name that can be used to provide an application server configuration in json format.
     *
     * @see ApplicationServerBuilder
     */
    public static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = ServerConfigurationReader.ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION;

    /**
     * The attribute name used for the delete-working-directory-on-shutdown flag definition in the application server's configuration json. The
     * attribute value is expected to be a boolean. Default is {@link #DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN}.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#keepWorkingDirectoryOnShutdown()
     * @see ApplicationServerBuilder#deleteWorkingDirectoryOnShutdown()
     */
    public static final String CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = "delete-working-directory-on-shutdown";

    /**
     * The attribute name used for the host definition in the application server's configuration json. Attribute values are
     * strings representing a valid host name. Default is {@link #DEFAULT_HOST}.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withHost(String)
     */
    public static final String CONFIGURATION_ATTRIBUTE_HOST = "host";

    /**
     * The attribute name used for the port definition in the application server's configuration json. Appropriate values are
     * integers within the range of allowed ports of the underlying system. If not specified the server will choose a port randomly.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withPort(int)
     */
    public static final String CONFIGURATION_ATTRIBUTE_PORT = "port";

    /**
     * The attribute name used for the session-timeout definition in the application server's configuration json. Attribute values are
     * expected to be integers that denote the session timeout in minutes. The session timeout is the time in minutes a session is kept alive
     * without any request from the client. After the timeout the session is invalidated and all session data is lost. The session timeout
     * is a server wide setting and affects all sessions. The default value is {@link #DEFAULT_SESSION_TIMEOUT}. Zero or negative values
     * denote that the session lifetime is unlimited. Default is {@link #DEFAULT_SESSION_TIMEOUT}.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withSessionTimeout(int)
     */
    public static final String CONFIGURATION_ATTRIBUTE_SESSION_TIMEOUT = "session-timeout";

    /**
     * The attribute name used for the working-directory definition in the application server's configuration json. Attribute values are
     * expected to denote an existing directory on disk.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withWorkingDirectory(File) 
     */
    public static final String CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY = "working-directory";
    
    /**
     * The attribute name used for the show-start-info definition in the application server's configuration json. The
     * attribute value is expected to be a boolean. Default is {@link #DEFAULT_SHOW_START_INFO}.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withStartInfoProvider(Function) 
     */
    public static final String CONFIGURATION_ATTRIBUTE_SHOW_START_INFO = "show-start-info";

    /**
     * The attribute name used for the secure-socket-layer definition in the application server's configuration json. Attribute values are
     * expected to be a json map that contains the secure socket layer configuration attributes.
     * <p>Example:</p>
     * <pre>
     *     com.codeaffine.tiny.star.configuration
     *       ={"secure-socket-layer":{"keystore-location":"classpath:keystore.jks", "keystore-password":"store-password", "key-alias":"alias", "key-password":"key-password"}}
     * </pre>
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withSecureSocketLayerConfiguration(SecureSocketLayerConfiguration)
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_PASSWORD
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_ALIAS
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_PASSWORD
     */
    public static final String CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER = "secure-socket-layer";

    /**
     * The attribute name used for the keystore-location definition in the application server's configuration json. Attribute values are
     * expected to be a string that denotes the location of the key store file. The location type is separated from the key store file name by a colon.
     * <p>Example:</p>
     * <pre>
     *     com.codeaffine.tiny.star.configuration={"secure-socket-layer":{"keystore-location":"classpath:keystore.jks", ... }}
     *     com.codeaffine.tiny.star.configuration={"secure-socket-layer":{"keystore-location":"filesystem:src/test/resources/keystore.jks", ... }}
     * </pre>
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withSecureSocketLayerConfiguration(SecureSocketLayerConfiguration)
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER
     */
    public static final String CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION = "keystore-location";

    /**
     * The attribute name used for the keystore-password definition in the application server's configuration json. Attribute values are
     * expected to be a string that denotes the password of the key store.
     *
     * <p>Note: as passwords are sensitive information it is recommended to provide the password by an appropriate secret injection mechanism.</p>
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withSecureSocketLayerConfiguration(SecureSocketLayerConfiguration)
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION
     */
    public static final String CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_PASSWORD = "keystore-password";

    /**
     * The attribute name used for the key-alias definition in the application server's configuration json. Attribute values are
     * expected to be a string that denotes the alias of the key entry.
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withSecureSocketLayerConfiguration(SecureSocketLayerConfiguration)
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_PASSWORD
     */
    public static final String CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_ALIAS = "key-alias";

    /**
     * The attribute name used for the key-password definition in the application server's configuration json. Attribute values are
     * expected to be a string that denotes the password of the key entry.
     *
     * <p>Note: as passwords are sensitive information it is recommended to provide the password by an appropriate secret injection mechanism.</p>
     *
     * @see ApplicationServerBuilder
     * @see ApplicationServerBuilder#withSecureSocketLayerConfiguration(SecureSocketLayerConfiguration)
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION
     */
    public static final String CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_PASSWORD = "key-password";

    /**
     * Default value for {@link #CONFIGURATION_ATTRIBUTE_HOST}
     */
    public static final String DEFAULT_HOST = "localhost";

    /**
     * Default value for {@link #CONFIGURATION_ATTRIBUTE_SESSION_TIMEOUT}
     */
    public static final int DEFAULT_SESSION_TIMEOUT = 15;

    /**
     * Default value for {@link #CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN}
     */
    public static final boolean DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = true;

    /**
     * Default value for {@link #CONFIGURATION_ATTRIBUTE_SHOW_START_INFO}
     */
    public static final boolean DEFAULT_SHOW_START_INFO = true;

    /**
     * Value returned by {@link #getIdentifier()} if not specified otherwise by using the {@link #newApplicationServerBuilder(ApplicationConfiguration, String)}
     * builder factory method.
     */
    public static final String DEFAULT_APPLICATION_IDENTIFIER = ApplicationServer.class.getName().toLowerCase();

    @SuppressWarnings("CanBeFinal")
    static Logger logger = getLogger(ApplicationServer.class);

    ApplicationConfiguration applicationConfiguration;
    SecureSocketLayerConfiguration secureSocketLayerConfiguration;
    String host;
    int port;
    File workingDirectory;
    boolean deleteWorkingDirectoryOnShutdown;
    Function<ApplicationServer, String> startInfoProvider;
    @Singular
    List<Object> lifecycleListeners;
    String applicationIdentifier;
    @Singular
    List<FilterDefinition> filterDefinitions;
    int sessionTimeout;
    @Singular
    List<ServletContextListener> servletContextListeners;
    Class<? extends RWTServlet> rwtServletExtension;

    private final AtomicReference<ApplicationProcess> processHolder = new AtomicReference<>();

    /**
     * Configuration possibilities of the {@link #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION} attribute. The attribute value
     * has to specify the location type of the key store file. The location type is separated from the key store file name by a colon.
     * <p>Example:</p>
     * <pre>
     *     com.codeaffine.tiny.star.configuration={"secure-socket-layer":{"keystore-location":"classpath:keystore.jks", ... }}
     *     com.codeaffine.tiny.star.configuration={"secure-socket-layer":{"keystore-location":"filesystem:src/test/resources/keystore.jks", ... }}
     * </pre>
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION
     * @see #CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER
     */
    public enum KeyStoreLocationType {
        CLASSPATH,
        FILESYSTEM
    }
    /**
     * The lifecycle states of an application server instance passes through.
     */
    public enum State {
        /**
         * this state signals the transition of the application server from {@link #HALTED} to {@link #RUNNING}. In this lifecycle phase
         * the server attempts to allocate resources like communication port and working directory. In this phase the server cannot
         * be expected to process requests.
         */
        STARTING,
        /**
         * this state signals the working phase of the application server's lifecycle. Resources have been allocated and the server is ready
         * to process requests.
         */
        RUNNING,
        /**
         * this state signals the transition of the application server from {@link #RUNNING} to {@link #HALTED}. In this lifecycle phase
         * the server attempts to release resources like communication port and working directory. In this phase the server cannot
         * be expected to process requests.
         */
        STOPPING,
        /**
         * this state signals the final state of the application server's lifecycle. Resources have been released and the server does not
         * process requests.
         */
        HALTED
    }

    /**
     * Mark a method as a lifecycle state change listener which gets notified when an observed server enters the {@link State#STARTING} state.
     * Observer objects can be registered via the {@link ApplicationServerBuilder#withLifecycleListener(Object)} configuration method.
     */
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Starting {}

    /**
     * Mark a method as a lifecycle state change listener which gets notified when an observed server enters the {@link State#RUNNING} state.
     * Observer objects can be registered via the {@link ApplicationServerBuilder#withLifecycleListener(Object)} configuration method.
     */
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Started {}

    /**
     * Mark a method as a lifecycle state change listener which gets notified when an observed server enters the {@link State#STOPPING} state.
     * Observer objects can be registered via the {@link ApplicationServerBuilder#withLifecycleListener(Object)} configuration method.
     */
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Stopping {}

    /**
     * Mark a method as a lifecycle state change listener which gets notified when an observed server enters the {@link State#HALTED} state.
     * Observer objects can be registered via the {@link ApplicationServerBuilder#withLifecycleListener(Object)} configuration method.
     */
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Stopped {}

    private static class InternalApplicationServerBuilder { // NOSONAR
        // needed to make javadoc with lombok happy
    }

    /**
     * <p>The {@link ApplicationServerBuilder} is used to create and configure an instance of {@link ApplicationServer} via a fluent API.
     * At minimum, an {@link ApplicationConfiguration} must be provided to start the server.</p>
     *
     * <p><strong>Minimal Example:</strong></p>
     * <pre><code class="language-java">
     * ApplicationServerBuilder
     *     .newApplicationServerBuilder(new MyAppConfiguration())
     *     .build()
     *     .start();
     * </code></pre>
     *
     * <p><strong>Customizing the Server:</strong></p>
     * <pre><code class="language-java">
     * ApplicationServerBuilder
     *     .newApplicationServerBuilder(new MyAppConfiguration())
     *     .withPort(8080)
     *     .withHost("localhost")
     *     .withSessionTimeout(30)
     *     .withWorkingDirectory(Paths.get("./work"))
     *     .build()
     *     .start();
     * </code></pre>
     *
     * <p><strong>Configuration via Environment Variable:</strong></p>
     * <p>Most configuration options can be provided via the environment variable
     * {@link ApplicationServer#ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION}. This variable accepts a JSON representing an attribute map.</p>
     *
     * <p>Example:</p>
     * <pre>
     * com.codeaffine.tiny.star.configuration={"port":8080,"sessionTimeout":30}
     * </pre>
     *
     * <p>If multiple server instances run on the same machine, use identifiers
     * (see <code>{@link #newApplicationServerBuilder(ApplicationConfiguration, String)}</code>):</p>
     * <pre>
     * com.codeaffine.tiny.star.configuration={
     *     "com.codeaffine.tiny.demo.MyApp": {"port":8080},
     *     "com.codeaffine.tiny.demo.AnotherApp": {"port":9090}
     * }
     * </pre>
     *
     * <p>Values set programmatically will always override environment settings.</p>
     *
     * <p><strong>Programmatic Configuration with JSON:</strong></p>
     * <pre><code class="language-java">
     * String configJson = "{\"port\":8080, \"host\":\"localhost\"}";
     * ApplicationServerBuilder
     *     .newApplicationServerBuilder(new MyAppConfiguration())
     *     .withConfiguration(configJson)
     *     .build()
     *     .start();
     * </code></pre>
     *
     * @see ApplicationServer
     */
    @RequiredArgsConstructor(access = PRIVATE)
    public static class ApplicationServerBuilder {

        @NonNull
        private final InternalApplicationServerBuilder delegate;

        /**
         * Creates a new {@link ApplicationServerBuilder} instance that uses its configured parameters to create an {@link ApplicationServer} instance.
         *
         * @return a new {@link ApplicationServer} instance. Never {@code null}.
         */
        public ApplicationServer build() {
            return delegate.build();
        }

        /**
         * Specify the secure socket layer configuration to use. If not specified the server will not use a secure transport layer. Note that the
         * ssl handling is done by the underlying servlet engine. Therefore, it is important to verify that the chosen server binding supports
         * the given configuration as expected.
         *
         * @param secureSocketLayerConfiguration the secure socket layer configuration to use. Null will be interpreted as no secure socket layer usage.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified secure socket layer configuration set. Never {@code null}.
         */
        public ApplicationServerBuilder withSecureSocketLayerConfiguration(SecureSocketLayerConfiguration secureSocketLayerConfiguration) {
            return new ApplicationServerBuilder(delegate.withSecureSocketLayerConfiguration(secureSocketLayerConfiguration));
        }

        /**
         * Define a particular port to use. If not specified the server will choose a port randomly.
         *
         * @param port the port to use. Must be within the range of allowed ports of the underlying system.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified port set. Never {@code null}.
         * @see #CONFIGURATION_ATTRIBUTE_PORT
         */
        public ApplicationServerBuilder withPort(int port) {
            return new ApplicationServerBuilder(delegate.withPort(port));
        }

        /**
         * Define a particular host name to use. If not specified the server will use the {@link ApplicationServer#DEFAULT_HOST} value.
         *
         * @param host the host to use. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified host set. Never {@code null}.
         * @see #CONFIGURATION_ATTRIBUTE_HOST
         */
        public ApplicationServerBuilder withHost(@NonNull String host) {
            return new ApplicationServerBuilder(delegate.withHost(host));
        }

        /**
         * Define a working directory to use. If not specified the server will use a temporary directory.
         *
         * @param workingDirectory the working directory to use. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified working directory set. Never {@code null}.
         * @throws IllegalArgumentException if the specified working directory does not exist.
         * @see #CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY
         */
        public ApplicationServerBuilder withWorkingDirectory(@NonNull File workingDirectory) {
            return new ApplicationServerBuilder(delegate.withWorkingDirectory(workingDirectory));
        }

        /**
         * register the given  {@link FilterDefinition} to the application server instance to create. Filter-mappings will be added to the web application
         * in the order the filter definitions are registered.
         *
         * @param filterDefinition the filter definition to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified filter definition registered. Never {@code null}.
         */
        public ApplicationServerBuilder withFilterDefinition(@NonNull FilterDefinition filterDefinition) {
            return new ApplicationServerBuilder(delegate.withFilterDefinition(filterDefinition));
        }

        /**
         * register the given  {@link FilterDefinition}s to the application server instance to create. Filter-mappings will be added to the web application
         * in the order of the given filter definitions argument.
         *
         * @param filterDefinitions the filter definitions to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified filter definitions registered. Never {@code null}.
         */
        public ApplicationServerBuilder withFilterDefinitions(FilterDefinition ... filterDefinitions) {
            return new ApplicationServerBuilder(delegate.withFilterDefinitions(stream(filterDefinitions)
                .filter(Objects::nonNull)
                .toList()));
        }

        /**
         * register the given  {@link FilterDefinition}s to the application server instance to create. Filter-mappings will be added to the web application
         * in the order of the given filter definitions argument.
         *
         * @param filterDefinitions the filter definitions to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified filter definitions registered. Never {@code null}.
         */
        public ApplicationServerBuilder withFilterDefinitions(@NonNull List<FilterDefinition> filterDefinitions) {
            return new ApplicationServerBuilder(delegate.withFilterDefinitions(filterDefinitions));
        }

        /**
         * Register a lifecycle listener that will be notified about {@link State} changes of the {@link ApplicationServer}. The listener will be notified
         * via callback methods annotated by the {@link Starting}, {@link Started}, {@link Stopping}, or {@link Stopped} annotations. One method might be
         * annotated by multiple annotations.These methods have to be either parameterless or expect the {@link ApplicationServer} as single injection
         * parameter. A given {@link ApplicationServer} instance is not guaranteed to be in the state the annotated method observes when the callback gets
         * invoked as notification is done asynchronously.
         *
         * <p>Example:</p>
         * <pre><code class="language-java">
         *     &#64;Starting
         *     void captureStarting(ApplicationServer applicationServer) {
         *     // ...
         *     }
         * </code></pre>
         * <p>Listener instances are notified in the order they have been registered.</p>
         *
         * @param lifecycleListener the listener to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified lifecycle listener registered. Never {@code null}.
         */
        public ApplicationServerBuilder withLifecycleListener(@NonNull Object lifecycleListener) {
            return new ApplicationServerBuilder(delegate.withLifecycleListener(lifecycleListener ));
        }

        /**
         * Register multiple lifecycle listeners that will be notified about {@link State} changes of the {@link ApplicationServer}. The listeners will be
         * notified via callback methods annotated by the {@link Starting}, {@link Started}, {@link Stopping}, or {@link Stopped} annotations. These methods
         * have to be either parameterless or expect the {@link ApplicationServer} as single injection parameter.
         * <p>Example:</p>
         * <pre><code class="language-java">
         *     &#64;Starting
         *     void captureStarting(ApplicationServer applicationServer) {
         *     // ...
         *     }
         * </code></pre>
         * <p>Listeners are notified in the order they occur in the given listener List.</p>
         *
         * @param lifecycleListeners the listeners to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified lifecycle listeners registered. Never {@code null}.
         */
        public ApplicationServerBuilder withLifecycleListeners(List<Object> lifecycleListeners) {
            return new ApplicationServerBuilder(delegate.withLifecycleListeners(lifecycleListeners));
        }

        /**
         * Keep the working directory after shutdown. If not specified the server will delete the working directory on shutdown.
         *
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified delete working directory on shutdown flag reset. Never
         * {@code null}.
         * @see #CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN
         */
        public ApplicationServerBuilder keepWorkingDirectoryOnShutdown() {
            return new ApplicationServerBuilder(delegate.withDeleteWorkingDirectoryOnShutdown(false));
        }

        /**
         * Delete the working directory on shutdown. Default setting if not specified otherwise.
         *
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified delete working directory on shutdown flag set.
         * Never {@code null}.
         * @see #CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN
         */
        public ApplicationServerBuilder deleteWorkingDirectoryOnShutdown() {
            return new ApplicationServerBuilder(delegate.withDeleteWorkingDirectoryOnShutdown(true));
        }

        /**
         * Define a provider function for an info message shown on the console before the application server starts. If not specified the server will
         * show a default about message.
         *
         * @param startInfoProvider the provider function for the start info message. {@code null} will omit the info message on application
         *                          server start.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified start info provider set. Never {@code null}.
         * @see #CONFIGURATION_ATTRIBUTE_SHOW_START_INFO
         */
        public ApplicationServerBuilder withStartInfoProvider(Function<ApplicationServer, String> startInfoProvider) {
            return new ApplicationServerBuilder(delegate.withStartInfoProvider(startInfoProvider));
        }

        /**
         * Define the session timeout tu use in minutes. If not specified the server will use the {@link #DEFAULT_SESSION_TIMEOUT} value.
         * Zero or negative values denote that the session lifetime is unlimited. The session timeout is a server wide setting and affects all sessions.
         * Note that the session timeout is the time in minutes a session is kept alive without any request from the client. After the timeout the session
         * is invalidated and all session data is lost.
         *
         * @param sessionTimeout the session timeout to use in minutes.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified session timeout set. Never {@code null}.
         * @see #CONFIGURATION_ATTRIBUTE_SESSION_TIMEOUT
         */
        public ApplicationServerBuilder withSessionTimeout(int sessionTimeout) {
            return new ApplicationServerBuilder(delegate.withSessionTimeout(normalizeSessionTimeout(sessionTimeout)));
        }

        /**
         * Register the given {@link ServletContextListener} implementation.
         *
         * @param servletContextListener the {@link ServletContextListener} implementation to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified {@link ServletContextListener} implementation registered.
         * Never {@code null}.
         */
        public ApplicationServerBuilder withServletContextListener(ServletContextListener servletContextListener) {
            return new ApplicationServerBuilder(delegate.withServletContextListener(servletContextListener));
        }

        /**
         * Register the given list of{@link ServletContextListener} implementations.
         *
         * @param servletContextListeners the list of{@link ServletContextListener} implementations to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified list of{@link ServletContextListener} implementations
         * registered. Never {@code null}.
         */
        public ApplicationServerBuilder withServletContextListeners(List<ServletContextListener> servletContextListeners) {
            return new ApplicationServerBuilder(delegate.withServletContextListeners(servletContextListeners));
        }

        /**
         * Extension of {@link RWTServlet} used to run the RWT standalone application.
         *
         * @param rwtServletExtension Extension of {@link RWTServlet} used to run the RWT standalone application. Must not be {@code null}. Must provide a
         *                            public parameterless constructor for generic instance creation.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified {@link RWTServlet} extension registered. Never {@code null}.
         */
        public ApplicationServerBuilder withRwtServletExtension(Class<? extends RWTServlet> rwtServletExtension) {
            try {
                rwtServletExtension.getConstructor().newInstance();
            } catch (Exception cause) {
                throw new IllegalArgumentException(format("RwtServlet extension '%s' cannot be instantiated.", rwtServletExtension.getName()), cause);
            }
            return new ApplicationServerBuilder(delegate.withRwtServletExtension(rwtServletExtension));
        }

        /**
         * Allows to configure the application server by providing a json string that contains a map of name/value pairs for the attributes to configure.
         * Note that this method will override any configuration provided by the {@link #ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION} environment variable.
         * Note also that any subsequent calls to {@link ApplicationServerBuilder} attribute setter methods will override settings made by this method.
         *
         * @param configuration the configuration string. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified configuration set. Never {@code null}.
         */
        public ApplicationServerBuilder withConfiguration(String configuration) {
            SingleServerConfigurationReader configurator = new SingleServerConfigurationReader(() -> configuration, delegate.applicationConfiguration);
            return new ApplicationServerBuilder(configure(configurator, delegate));
        }

        /**
         * Allows to configure the application server by providing a json stream that contains a map of name/value pairs for the attributes to configure.
         * Note that this method will override any configuration provided by the {@link #ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION} environment variable.
         * Note also that any subsequent calls to {@link ApplicationServerBuilder} attribute setter methods will override settings made by this method.
         *
         * @param configuration the configuration stream. Must not be {@code null}. The encoding of the stream is expected to be UTF-8. The stream will
         *                      not be closed by this method.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified configuration set. Never {@code null}.
         */
        public ApplicationServerBuilder withConfiguration(InputStream configuration) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                configuration.transferTo(out);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            return withConfiguration(out.toString(UTF_8));
        }
    }

    /**
     * Create a new {@link ApplicationServerBuilder} instance representing the starting point of a fluent API configuration chain that eventually
     * uses the configured parameters to {@link ApplicationServerBuilder#build()} an {@link ApplicationServer} instance.
     *
     * @param applicationConfiguration the {@link ApplicationConfiguration} implementation that defines the RWT application to start. Must not be
     *                                 {@code null}.
     * @return a new {@link ApplicationServer} instance. Never {@code null}.
     * @see ApplicationServerBuilder
     */
    public static ApplicationServerBuilder newApplicationServerBuilder(@NonNull ApplicationConfiguration applicationConfiguration) {
        SingleServerConfigurationReader configurator = new SingleServerConfigurationReader(applicationConfiguration);
        return newApplicationServerBuilder(configurator, applicationConfiguration, DEFAULT_APPLICATION_IDENTIFIER);
    }

    /**
     * Create a new {@link ApplicationServerBuilder} instance representing the starting point of a fluent API configuration chain that eventually
     * uses the configured parameters to {@link ApplicationServerBuilder#build()} an {@link ApplicationServer} instance.
     *
     * @param applicationConfiguration the {@link ApplicationConfiguration} implementation that defines the RWT application to start. Must not be
     *                                 {@code null}.
     * @param applicationIdentifier   the identifier of the application server. Must not be {@code null}.
     * @return a new {@link ApplicationServer} instance. Never {@code null}.
     * @see ApplicationServerBuilder
     */
    public static ApplicationServerBuilder newApplicationServerBuilder(
        @NonNull ApplicationConfiguration applicationConfiguration,
        @NonNull String applicationIdentifier)
    {
        MultiServerConfigurationReader configurator = new MultiServerConfigurationReader(applicationIdentifier, applicationConfiguration);
        return newApplicationServerBuilder(configurator, applicationConfiguration, applicationIdentifier);
    }

    private static ApplicationServerBuilder newApplicationServerBuilder(
        ServerConfigurationReader configurator,
        ApplicationConfiguration applicationConfiguration,
        String applicationIdentifier)
    {
        InternalApplicationServerBuilder internalApplicationServerBuilder = newDefaultApplicationServerBuilder()
            .withApplicationConfiguration(applicationConfiguration)
            .withApplicationIdentifier(applicationIdentifier);
        return new ApplicationServerBuilder(configure(configurator, internalApplicationServerBuilder));
    }

    private static InternalApplicationServerBuilder configure(
        ServerConfigurationReader configurator,
        InternalApplicationServerBuilder internalApplicationServerBuilder)
    {
        return internalApplicationServerBuilder
            .withSecureSocketLayerConfiguration(configurator.readSecureSocketLayerConfiguration())
            .withHost(configurator.readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_HOST, DEFAULT_HOST, String.class))
            .withPort(configurator.readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_PORT, findFreePort(), Integer.class))
            .withWorkingDirectory(configurator.readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY, null, File::new))
            .withStartInfoProvider(
                TRUE.equals(configurator.readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_SHOW_START_INFO, TRUE, Boolean.class))
                    ? applicationServer -> format(TINY_STAR_START_INFO, applicationServer.getVersion(), applicationServer.getIdentifier(), now().getYear())
                    : null)
            .withDeleteWorkingDirectoryOnShutdown(
                configurator.readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                    DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                    Boolean.class))
            .withSessionTimeout(normalizeSessionTimeout(configurator.readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_SESSION_TIMEOUT,
                DEFAULT_SESSION_TIMEOUT,
                Integer.class)));
    }

    /**
     * returns the identifier of the application server. Can be specified by the {@link #newApplicationServerBuilder(ApplicationConfiguration, String)} method.
     * If not specified otherwise the identifier is the value of the {@link #DEFAULT_APPLICATION_IDENTIFIER} constant. While the latter is sufficient for
     * simple use cases it is not suitable if multiple application server instances are running on the same machine and particular file mappings infos (e.g.
     * to the server's working directory) are needed.
     *
     * @return the identifier of the application server. Never {@code null}.
     */
    public String getIdentifier() {
        return applicationIdentifier;
    }

    /**
     * returns the version of the tiny star application server library.
     *
     * @return the version of the tiny star application server library. Never {@code null}.
     */
    public String getVersion() {
        String result = ApplicationServer.class.getPackage().getImplementationVersion();
        if (isNull(result)) {
            result = "not available";
        }
        return result;
    }

    /**
     * returns the current lifecycle {@link State} of the application server.
     *
     * @return the current lifecycle {@link State} of the application server. Never {@code null}.
     */
    public State getState() {
        ApplicationProcess applicationProcess = processHolder.get();
        return isNull(applicationProcess) ? HALTED :  applicationProcess.getState();
    }

    /**
     * returns the {@link URL}s to the RWT application's entry points. The entry points are defined by the {@link ApplicationConfiguration} implementation
     * that has been specified by the {@link ApplicationServer#newApplicationServerBuilder(ApplicationConfiguration)} method.
     *
     * @return the {@link URL}s to the RWT application's entry points. Never {@code null}.
     */
    public URL[] getUrls() {
        return captureEntrypointPaths(applicationConfiguration)
            .stream()
            .map(this::toUrl)
            .toArray(URL[]::new);
    }

    /**
     * start this {@link ApplicationServer} instance with the {@link ApplicationConfiguration} implementation and configuration settings specified by the
     * {@link ApplicationServerBuilder} instance that has been used to create this {@link ApplicationServer} instance. Does nothing if the application
     * server is already running.
     *
     * @return this {@link ApplicationServer} instance. Never {@code null}.
     */
    public ApplicationServer start() {
        return startInternal(new ApplicationProcessFactory(this));
    }

    /**
     * stop this {@link ApplicationServer} instance. Does nothing if the application server is already stopped.
     *
     * @return this {@link ApplicationServer} instance. Never {@code null}.
     */
    public ApplicationServer stop() {
        return stopInternal();
    }

    String getWorkingDirectorSystemProperty() {
        return getIdentifier() + "." + CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY;
    }

    ApplicationServer stopInternal() {
        ApplicationProcess process = processHolder.getAndUpdate(currentProcess -> null);
        if (nonNull(process)) {
            logger.info(INFO_SHUTDOWN_START, getIdentifier());
            measureDuration(process::stop)
                .report(duration -> logger.info(INFO_SHUTDOWN_CONFIRMATION, getIdentifier(), duration));
        }
        return this;
    }

    ApplicationServer startInternal(ApplicationProcessFactory applicationProcessFactory) {
        ApplicationProcess process = processHolder.updateAndGet(currentProcess -> createProcess(currentProcess, applicationProcessFactory));
        if (process.getState().equals(HALTED)) {
            measureDuration(process::start)
                .report(this::logStartupInfos);
        }
        return this;
    }

    private ApplicationProcess createProcess(ApplicationProcess currentProcess, ApplicationProcessFactory applicationProcessFactory) {
        if (nonNull(currentProcess)) {
            return currentProcess;
        }
        return measureDuration(applicationProcessFactory::createProcess)
            .report((value, duration) -> logger.info(INFO_CREATION_CONFIRMATION, getIdentifier(), duration));
    }

    private void logStartupInfos(long duration) {
        stream(getUrls())
            .forEach(url -> logger.info(INFO_ENTRYPOINT_URL, url.toString()));
        logger.info(INFO_STARTUP_CONFIRMATION, getIdentifier(), duration);
    }

    private URL toUrl(String path) {
        try {
            String scheme = isNull(secureSocketLayerConfiguration) ? "http" : "https";
            URI uri = new URI(scheme, null, host, port, path, null, null);
            return uri.toURL();
        } catch (URISyntaxException | MalformedURLException cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    private static int normalizeSessionTimeout(Integer sessionTimeout) {
        return max(sessionTimeout, 0);
    }
}
