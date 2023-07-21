/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.Protocol;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.codeaffine.tiny.shared.IoUtils.findFreePort;
import static com.codeaffine.tiny.shared.Metric.measureDuration;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static com.codeaffine.tiny.star.ServerConfigurationReader.readEnvironmentConfigurationAttribute;
import static com.codeaffine.tiny.star.Texts.*;
import static com.codeaffine.tiny.star.spi.Protocol.HTTP;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static lombok.Builder.Default;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <p>An {@link ApplicationServer} is a {@link jakarta.servlet.http.HttpServlet} engine adapter that controls the lifecycle of standalone RWT applications.
 * It allows to {@link #start()} and {@link #stop()} {@link org.eclipse.rap.rwt.application.Application} instances defined by {@link ApplicationConfiguration}
 * implementations.</p>
 * <p>The {@link ApplicationServer} notifies lifecycle listeners about {@link State} changes. Listeners receive notifications via callback methods
 * annotated by the {@link Starting}, {@link Started}, {@link Stopping}, or {@link Stopped} annotations. These methods have to be either parameterless or
 * expect the {@link ApplicationServer} as single injection parameter.</p>
 * <p>Use {@link #newApplicationServerBuilder(ApplicationConfiguration)} to create and configure an instance of this class.</p>
 * <p>Example:</p>
 * <pre>
 *     public static void main(String[] args) {
 *         newApplicationServerBuilder(new DemoApplicationConfiguration())
 *             .withLifecycleListener(new DemoLifecycleListener())
 *             .withApplicationIdentifier("com.codeaffine.tiny.star.demo.DemoApplication")
 *             .build()
 *             .start();
 *     }
 * </pre>
 * <p>See {@link ApplicationServerBuilder} for configuration details.</p>
 * <p>The actual servlet engine is provided as a service that implements the {@link com.codeaffine.tiny.star.spi.Server} interface. Clients specify
 * an appropriate runtime dependency to use one of the available server implementations. Note that the server implementations are not part of the public API.
 * They may be subject to change without notice. Note also that only one server implementation at a time must be available on the module-/classpath.</p>
 * <p>Providing an own server implementation is possible by implementing the {@link com.codeaffine.tiny.star.spi.Server} interface and registering it as a
 * service via the {@link java.util.ServiceLoader} mechanism using an {@link com.codeaffine.tiny.star.spi.ServerFactory}. Ensure that the implementation
 * passes the contract tests provided by the com.codeaffine.tiny.star.tck module.</p>
 */
@Builder(
    builderClassName = "InternalApplicationServerBuilder",
    builderMethodName = "newDefaultApplicationServerBuilder",
    setterPrefix = "with",
    access = PRIVATE
)
public class ApplicationServer {

    /**
     * The environment variable name for the application server configuration json.
     *
     * @see ApplicationServerBuilder
     */
    public static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = ServerConfigurationReader.ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION;
    public static final String CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = "delete-working-directory-on-shutdown";
    public static final String CONFIGURATION_ATTRIBUTE_PROTOCOL = "http";
    public static final String CONFIGURATION_ATTRIBUTE_HOST = "host";
    public static final String CONFIGURATION_ATTRIBUTE_PORT = "port";
    public static final String CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY = "working-directory";
    public static final String CONFIGURATION_ATTRIBUTE_SHOW_START_INFO = "show-start-info";
    public static final Protocol DEFAULT_PROTOCOL = HTTP;
    public static final String DEFAULT_HOST = "localhost";
    public static final boolean DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = true;
    public static final String SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY = "com.codeaffine.tiny.star.workingDirectory";

    private static final String ILLEGAL_FILENAME_CHARACTERS = "[^a-zA-Z0-9.\\-]";

    /**
     * The {@link ApplicationConfiguration} implementation that defines the RAP/RWT application to start. Mandatory, must not be {@code null}.
     */
    @NonNull
    final ApplicationConfiguration applicationConfiguration;
    @NonNull
    @Default
    final Protocol protocol = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_PROTOCOL, DEFAULT_PROTOCOL, Protocol::valueOf);
    @Default
    final String host = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_HOST, DEFAULT_HOST, String.class);
    @Default
    final int port = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_PORT, findFreePort(), Integer.class);
    @Default
    final File workingDirectory = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY, null, File::new);
    @Default
    final boolean deleteWorkingDirectoryOnShutdown
        = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                                                DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                                                Boolean.class);
    @Default
    final Function<ApplicationServer, String> startInfoProvider
        =   TRUE.equals(readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_SHOW_START_INFO, TRUE, Boolean.class))
          ? applicationServer -> format(TINY_STAR_START_INFO, applicationServer.getIdentifier(), now().getYear())
          : null;
    @Singular
    final List<Object> lifecycleListeners;
    final String applicationIdentifier;

    private final AtomicReference<ApplicationProcess> processHolder = new AtomicReference<>();

    public enum State { STARTING, RUNNING, STOPPING, HALTED }

    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Starting {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Started {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Stopping {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Stopped {}

    /**
     * <p>The {@link ApplicationServerBuilder} allows to configure and create an instance of {@link ApplicationServer}. The builder uses a fluent API
     * paradigm for concise configuration. The attribute setter methods mostly use the {@code with} prefix followed by the attribute name and return
     * the builder instance itself which facilitates the easy to read fluent attribute assignments. The actual application server instance is created by
     * calling the {@link ApplicationServerBuilder}'s build method. Note that at least the {@link ApplicationConfiguration} must be specified to start
     * the application server. Therefore, the {@link #newApplicationServerBuilder(ApplicationConfiguration)} method is the starting point of the
     * configuration chain.</p>
     * <p>Most configuration attributes may be set by an environment variable. To do so specify the ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION
     * environment variable at execution time. The variable's value exists of a json that contains the name/value map for the attributes to configure.</p>
     * <p>Example configuration that specifies a particular port to use:</p>
     * <pre>
     *     com.codeaffine.tiny.star.configuration={"port":12000}
     * </pre>
     * <p>Setting an attribute value programmatically will override the value provided by the environment variable.</p>
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
         * Define a particular port to use. If not specified the server will use a random port.
         *
         * @param port the port to use. Must be within the range of allowed ports of the underlying system.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified port set. Never {@code null}.
         */
        public ApplicationServerBuilder withPort(int port) {
            return new ApplicationServerBuilder(delegate.withPort(port));
        }

        /**
         * Define a particular host name to use. If not specified the server will use the {@link ApplicationServer#DEFAULT_HOST} value.
         *
         * @param host the host to use. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified host set. Never {@code null}.
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
         */
        public ApplicationServerBuilder withWorkingDirectory(@NonNull File workingDirectory) {
            return new ApplicationServerBuilder(delegate.withWorkingDirectory(workingDirectory));
        }

        /**
         * Define an identifier for the application server. If not specified the server will use an identifier derived of the {@link ApplicationConfiguration}
         * class name.
         *
         * @param applicationIdentifier the identifier to use. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified identifier set. Never {@code null}.
         */
        public ApplicationServerBuilder withApplicationIdentifier(@NonNull String applicationIdentifier) {
            return new ApplicationServerBuilder(delegate.withApplicationIdentifier(applicationIdentifier));
        }

        /**
         * Register a lifecycle listener that will be notified about {@link State} changes of the {@link ApplicationServer}. The listener will be notified
         * via callback methods annotated by the {@link Starting}, {@link Started}, {@link Stopping}, or {@link Stopped} annotations. These methods have to
         * be either parameterless or expect the {@link ApplicationServer} as single injection parameter.
         * <p>Example:</p>
         * <pre>
         *     &#64;Starting
         *     void captureStarting(ApplicationServer applicationServer) {
         *     // ...
         *     }
         * </pre>
         * <p>Listeners are notified in the order they have been registered.</p>
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
         * <pre>
         *     &#64;Starting
         *     void captureStarting(ApplicationServer applicationServer) {
         *     // ...
         *     }
         * </pre>
         * <p>Listeners are notified in the order they occur in the given listener List.</p>
         *
         * @param lifecycleListeners the listeners to register. Must not be {@code null}.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified lifecycle listeners registered. Never {@code null}.
         */
        public ApplicationServerBuilder withLifecycleListeners(List<Object> lifecycleListeners) {
            return new ApplicationServerBuilder(delegate.withLifecycleListeners(lifecycleListeners));
        }

        /**
         * Define whether the working directory should be deleted on shutdown. If not specified the server will delete the working directory on shutdown.
         *
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified delete working directory on shutdown flag set. Never
         * {@code null}.
         */
        public ApplicationServerBuilder keepWorkingDirectoryOnShutdown() {
            return new ApplicationServerBuilder(delegate.withDeleteWorkingDirectoryOnShutdown(false));
        }

        /**
         * Define a provider function for an info message shown on the console before the application server starts. If not specified the server will
         * show a default about message.
         *
         * @param startInfoProvider the provider function for the start info message. {@code null} will omit the info message on application
         *                          server start.
         * @return a clone of this {@link ApplicationServerBuilder} instance having the specified start info provider set. Never {@code null}.
         */
        public ApplicationServerBuilder withStartInfoProvider( Function<ApplicationServer, String> startInfoProvider) {
            return new ApplicationServerBuilder(delegate.withStartInfoProvider(startInfoProvider));
        }
    }

    /**
     * Create a new {@link ApplicationServerBuilder} instance representing the starting point of a fluent API configuration chain that eventually
     * uses the configured parameters to {@link ApplicationServerBuilder#build()} an {@link ApplicationServer} instance.
     * <p>Example:</p>
     * <pre>
     *     public static void main(String[] args) {
     *         newApplicationServerBuilder(new DemoApplicationConfiguration())
     *             .withLifecycleListener(new DemoLifecycleListener())
     *             .withApplicationIdentifier("com.codeaffine.tiny.star.demo.DemoApplication")
     *             .build()
     *             .start();
     *     }
     * </pre>
     *
     * @param applicationConfiguration the {@link ApplicationConfiguration} implementation that defines the RWT application to start. Must not be
     *                                 {@code null}.
     * @return a new {@link ApplicationServer} instance. Never {@code null}.
     * @see ApplicationServerBuilder
     */
    public static ApplicationServerBuilder newApplicationServerBuilder(@NonNull ApplicationConfiguration applicationConfiguration) {
        return new ApplicationServerBuilder(newDefaultApplicationServerBuilder()
            .withApplicationConfiguration(applicationConfiguration));
    }

    /**
     * returns the identifier of the application server. Can be specified by the {@link ApplicationServerBuilder#withApplicationIdentifier(String)} method.
     *
     * @return the identifier of the application server. Never {@code null}.
     */
    public String getIdentifier() {
        return isNull(applicationIdentifier) ? encode(applicationConfiguration.getClass().getName()) : applicationIdentifier;
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
        return startInternal(new ApplicationProcessFactory(this), getLogger(getClass()));
    }

    /**
     * stop this {@link ApplicationServer} instance. Does nothing if the application server is already stopped.
     *
     * @return this {@link ApplicationServer} instance. Never {@code null}.
     */
    public ApplicationServer stop() {
        return stopInternal(getLogger(getClass()));
    }

    ApplicationServer stopInternal(Logger logger) {
        ApplicationProcess process = processHolder.getAndUpdate(currentProcess -> null);
        if (nonNull(process)) {
            logger.info(INFO_SHUTDOWN_START, getIdentifier());
            measureDuration(process::stop)
                .report(duration -> logger.info(INFO_SHUTDOWN_CONFIRMATION, getIdentifier(), duration));
        }
        return this;
    }

    ApplicationServer startInternal(ApplicationProcessFactory applicationProcessFactory, Logger logger) {
        ApplicationProcess process = processHolder.updateAndGet(currentProcess -> createProcess(logger, currentProcess, applicationProcessFactory));
        if (process.getState().equals(HALTED)) {
            measureDuration(process::start)
                .report(duration -> logStartupInfos(logger, duration));
        }
        return this;
    }

    private ApplicationProcess createProcess(Logger logger, ApplicationProcess currentProcess, ApplicationProcessFactory applicationProcessFactory) {
        if (nonNull(currentProcess)) {
            return currentProcess;
        }
        return measureDuration(applicationProcessFactory::createProcess)
            .report((value, duration) -> logger.info(INFO_CREATION_CONFIRMATION, getIdentifier(), duration));
    }

    private void logStartupInfos(Logger logger, long duration) {
        stream(getUrls())
            .forEach(url -> logger.info(INFO_ENTRYPOINT_URL, url.toString()));
        logger.info(INFO_STARTUP_CONFIRMATION, getIdentifier(), duration);
    }

    private URL toUrl(String path) {
        try {
            URI uri = new URI(protocol.name().toLowerCase(), null, host, port, path, null, null);
            return uri.toURL();
        } catch (URISyntaxException | MalformedURLException cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    private static String encode(String name) {
        return name.replaceAll(ILLEGAL_FILENAME_CHARACTERS, "_");
    }
}
