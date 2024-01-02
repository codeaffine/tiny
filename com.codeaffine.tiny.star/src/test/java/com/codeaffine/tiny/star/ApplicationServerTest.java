/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.ServerConfigurationAssert;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.codeaffine.tiny.shared.IoUtils.deleteDirectory;
import static com.codeaffine.tiny.shared.IoUtils.findFreePort;
import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.State.*;
import static com.codeaffine.tiny.star.ApplicationServerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.star.ApplicationServerTestContext.getCurrentServerConfiguration;
import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static com.codeaffine.tiny.star.Texts.*;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationServerTestContext.class)
class ApplicationServerTest {

    private static final String APPLICATION_IDENTIFIER = "applicationIdentifier";
    private static final int CUSTOM_PORT = Integer.MAX_VALUE;
    private static final String CUSTOM_HOST = "host";
    private static final String ENTRY_POINT_PATH_1 = "/ep1";
    private static final String ENTRY_POINT_PATH_2 = "/ep2";
    private static final String SCHEME = "http";

    private static final ApplicationConfiguration MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION = application -> {
        application.addEntryPoint(ENTRY_POINT_PATH_1, () -> null, null);
        application.addEntryPoint(ENTRY_POINT_PATH_2, () -> null, null);
    };

    private ApplicationServer applicationServer;
    private File persistentWorkingDirectory;
    private Logger logger;
    @TempDir
    private File tempDir;
    
    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
    }

    @AfterEach
    void tearDown() {
        if(nonNull(applicationServer)) {
            applicationServer.stop();
        }
        if(nonNull(persistentWorkingDirectory) && persistentWorkingDirectory.exists()) {
            deleteDirectory(persistentWorkingDirectory);
        }
    }

    @Test
    void getUrls() throws MalformedURLException, URISyntaxException {
        int port = findFreePort();
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withPort(port)
            .build();

        URL[] actual = applicationServer.getUrls();

        assertThat(actual).containsExactlyInAnyOrder(
            new URI(SCHEME, null, DEFAULT_HOST, port, ENTRY_POINT_PATH_1, null, null).toURL(),
            new URI(SCHEME, null, DEFAULT_HOST, port, ENTRY_POINT_PATH_2, null, null).toURL()
        );
    }

    @Test
    void start() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        File workingDirectory = new File(getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        ArgumentCaptor<String> applicationIdentifierCaptor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        order.verify(logger).info(eq(INFO_SERVER_USAGE), applicationIdentifierCaptor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verify(logger).info(eq(INFO_CREATION_CONFIRMATION), eq(applicationIdentifierCaptor.getValue()), anyLong());
        order.verify(logger).info(INFO_ENTRYPOINT_URL, expectedEntrypointUrl(ENTRY_POINT_PATH_1));
        order.verify(logger).info(INFO_ENTRYPOINT_URL, expectedEntrypointUrl(ENTRY_POINT_PATH_2));
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(applicationIdentifierCaptor.getValue()), anyLong());
        order.verifyNoMoreInteractions();
        assertThat(CURRENT_SERVER.get().isStarted()).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isFalse();
        ServerConfigurationAssert.assertThat(getCurrentServerConfiguration())
            .hasHost(DEFAULT_HOST)
            .hasNonNegativePort()
            .hasContextClassLoader(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION.getClass().getClassLoader())
            .hasEntryPointPaths(captureEntrypointPaths(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION))
            .hasSessionTimeout(DEFAULT_SESSION_TIMEOUT)
            .hasWorkingDirectory(workingDirectory)
            .hasExistingWorkingDirectory();
        assertThat(applicationIdentifierCaptor.getAllValues())
            .allMatch(DEFAULT_APPLICATION_IDENTIFIER::equals);
    }

    @Test
    void startWithWorkingDirectoryThatExists() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        applicationServer.start();

        File createdWorkingDirectory = new File(getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        assertThat(isCreated).isTrue();
        assertThat(getCurrentServerConfiguration().getWorkingDirectory())
            .isEqualTo(givenWorkingDirectory)
            .isEqualTo(createdWorkingDirectory)
            .isDirectory()
            .exists();
    }

    @Test
    void startWithWorkingDirectoryThatDoesNotExist() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        ApplicationServer server = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        Exception actual = catchException(server::start);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(givenWorkingDirectory.getAbsolutePath());
        assertThat(CURRENT_SERVER.get()).isNull();
        assertThat(getProperty(server.getWorkingDirectorSystemProperty())).isNull();
    }

    @Test
    void startWithWorkingDirectoryThatIsNoDirectory() throws IOException {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean fileCreated = givenWorkingDirectory.createNewFile();
        ApplicationServer server = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        Exception actual = catchException(server::start);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(givenWorkingDirectory.getAbsolutePath());
        assertThat(fileCreated).isTrue();
        assertThat(CURRENT_SERVER.get()).isNull();
        assertThat(getProperty(server.getWorkingDirectorSystemProperty())).isNull();
    }

    @Test
    void startWithPort() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withPort(CUSTOM_PORT)
            .build();

        applicationServer.start();

        assertThat(getCurrentServerConfiguration().getPort()).isEqualTo(CUSTOM_PORT);
    }

    @Test
    void startWithHost() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withHost(CUSTOM_HOST)
            .build();

        applicationServer.start();

        assertThat(getCurrentServerConfiguration().getHost()).isEqualTo(CUSTOM_HOST);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "1, 1",
        "42, 42",
        "-1, 0"
    })
    void startWithSessionTimeout(int sessionTimeout, int expectedSessionTimeout) {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withSessionTimeout(sessionTimeout)
            .build();

        applicationServer.start();

        ServerConfigurationAssert.assertThat(getCurrentServerConfiguration()).hasSessionTimeout(expectedSessionTimeout);
    }

    @Test
    void startWithoutDeletingWorkingDirectoryOnShutdown() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .keepWorkingDirectoryOnShutdown()
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        persistentWorkingDirectory = new File(getProperty(applicationServer.getWorkingDirectorSystemProperty()));

        applicationServer.stop();

        assertThat(getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNull();
        assertThat(getCurrentServerConfiguration().getWorkingDirectory())
            .exists()
            .isEqualTo(persistentWorkingDirectory);
    }

    @Test
    void startWithApplicationIdentifier() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION, APPLICATION_IDENTIFIER)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        File workingDirectory = new File(getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        ArgumentCaptor<String> captor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        order.verify(logger).info(eq(INFO_SERVER_USAGE), captor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(captor.getValue()), anyLong());
        order.verifyNoMoreInteractions();
        assertThat(captor.getValue()).isEqualTo(APPLICATION_IDENTIFIER);
        assertThat(workingDirectory.getName()).startsWith(APPLICATION_IDENTIFIER);
        assertThat(getCurrentServerConfiguration().getWorkingDirectory())
            .isEqualTo(workingDirectory)
            .isDirectory()
            .exists();
    }

    @Test
    void startWithLifecycleListener() {
        StateCaptor stateCaptor = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withLifecycleListener(stateCaptor)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        InOrder order = inOrder(stateCaptor);
        order.verify(stateCaptor).captureStarting(applicationServer);
        order.verify(stateCaptor).captureStarted(applicationServer);
        order.verifyNoMoreInteractions();
        assertThat(stateCaptor.getHalted()).isNull();
        assertThat(stateCaptor.getStarting()).isSameAs(STARTING);
        assertThat(stateCaptor.getStarted()).isSameAs(RUNNING);
        assertThat(stateCaptor.getStopping()).isNull();
    }

    @Test
    void startWithMultipleLifecycleListeners() {
        StateCaptor stateCaptor1 = spy(new StateCaptor());
        StateCaptor stateCaptor2 = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withLifecycleListener(stateCaptor1)
            .withLifecycleListener(stateCaptor2)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        InOrder order = inOrder(stateCaptor1, stateCaptor2);
        order.verify(stateCaptor1).captureStarting(applicationServer);
        order.verify(stateCaptor2).captureStarting(applicationServer);
        order.verify(stateCaptor1).captureStarted(applicationServer);
        order.verify(stateCaptor2).captureStarted(applicationServer);
        order.verifyNoMoreInteractions();
    }

    @Test
    void startWithMultipleLifecycleListenersRegisteredAtOnce() {
        StateCaptor stateCaptor1 = spy(new StateCaptor());
        StateCaptor stateCaptor2 = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withLifecycleListeners(List.of(stateCaptor1, stateCaptor2))
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        InOrder order = inOrder(stateCaptor1, stateCaptor2);
        order.verify(stateCaptor1).captureStarting(applicationServer);
        order.verify(stateCaptor2).captureStarting(applicationServer);
        order.verify(stateCaptor1).captureStarted(applicationServer);
        order.verify(stateCaptor2).captureStarted(applicationServer);
        order.verifyNoMoreInteractions();
    }

    @Test
    void startWithConfigurationWithJsonString() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withConfiguration(format("{\"port\": %s}", CUSTOM_PORT))
            .build();

        applicationServer.start();

        assertThat(getCurrentServerConfiguration().getPort()).isEqualTo(CUSTOM_PORT);
    }

    @Test
    void startWithConfigurationWithJsonInputStream() throws IOException {
        ByteArrayInputStream configuration = spy(new ByteArrayInputStream(format("{\"port\": %s}", CUSTOM_PORT).getBytes(StandardCharsets.UTF_8)));
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withConfiguration(configuration)
            .build();

        applicationServer.start();

        assertThat(getCurrentServerConfiguration().getPort()).isEqualTo(CUSTOM_PORT);
        verify(configuration, never()).close();
    }

    @Test
    void startIfAlreadyStarted() {
        StateCaptor stateCaptor = new StateCaptor();
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withLifecycleListener(stateCaptor)
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        stateCaptor.clear();

        applicationServer.start();

        assertThat(stateCaptor.getHalted()).isNull();
        assertThat(stateCaptor.getStarting()).isNull();
        assertThat(stateCaptor.getStarted()).isNull();
        assertThat(stateCaptor.getStopping()).isNull();
    }

    @Test
    void stop() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .build();

        ApplicationServer actual = applicationServer.stop();

        assertThat(actual).isSameAs(applicationServer);
    }

    @Test
    void stopRunningInstance() {
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        reset(logger);

        applicationServer.stopInternal(logger);

        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
        assertThat(getCurrentServerConfiguration().getWorkingDirectory()).doesNotExist();
        assertThat(getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNull();
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_SHUTDOWN_START, applicationServer.getIdentifier());
        order.verify(logger).info(eq(INFO_SHUTDOWN_CONFIRMATION), eq(applicationServer.getIdentifier()), anyLong());
        order.verifyNoMoreInteractions();
    }

    @Test
    void stopRunningInstanceButKeepWorkingDirectory() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withWorkingDirectory(givenWorkingDirectory)
            .keepWorkingDirectoryOnShutdown()
            .build()
            .start();
        reset(logger);

        applicationServer.stop();

        assertThat(getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNull();
        assertThat(isCreated).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
        assertThat(getCurrentServerConfiguration().getWorkingDirectory())
            .isEqualTo(givenWorkingDirectory)
            .exists();
    }

    @Test
    void stopWithLifecycleListener() {
        StateCaptor stateCaptor = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withLifecycleListener(stateCaptor)
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        reset(stateCaptor);
        stateCaptor.clear();

        applicationServer.stop();

        InOrder order = inOrder(stateCaptor);
        order.verify(stateCaptor).captureStopping(applicationServer);
        order.verify(stateCaptor).captureHalted(applicationServer);
        order.verifyNoMoreInteractions();
        assertThat(stateCaptor.getHalted()).isSameAs(HALTED);
        assertThat(stateCaptor.getStarting()).isNull();
        assertThat(stateCaptor.getStarted()).isNull();
        assertThat(stateCaptor.getStopping()).isSameAs(HALTED);
    }

    @Test
    void stopIfAlreadyStopped() {
        StateCaptor stateCaptor = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(MULTI_ENTRYPOINT_APPLICATION_CONFIGURATION)
            .withLifecycleListener(stateCaptor)
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        reset(stateCaptor);
        stateCaptor.clear();
        applicationServer.stop();
        stateCaptor.clear();

        applicationServer.stop();

        assertThat(stateCaptor.getHalted()).isNull();
        assertThat(stateCaptor.getStarting()).isNull();
        assertThat(stateCaptor.getStarted()).isNull();
        assertThat(stateCaptor.getStopping()).isNull();
    }

    @Test
    void newApplicationRunnerBuilderWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> newApplicationServerBuilder(null))
            .isInstanceOf(NullPointerException.class);
    }

    private static String expectedEntrypointUrl(String entryPointPath) {
        return format("%s://%s:%s%s", SCHEME, DEFAULT_HOST, getCurrentServerConfiguration().getPort(), entryPointPath);
    }
}
