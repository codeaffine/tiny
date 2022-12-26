package com.codeaffine.tiny.star;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationServer.State.STARTING;
import static com.codeaffine.tiny.star.ApplicationServerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.star.IoUtils.deleteDirectory;
import static com.codeaffine.tiny.star.Texts.*;
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

    private ApplicationConfiguration applicationConfiguration;
    private ApplicationServer applicationServer;
    private File persistentWorkingDirectory;
    private Logger logger;
    @TempDir
    private File tempDir;


    @BeforeEach
    void setUp() {
        applicationConfiguration = application -> {};
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
    void start() {
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        File workingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        ArgumentCaptor<String> applicationIdentifierCaptor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        order.verify(logger).info(eq(INFO_SERVER_USAGE), applicationIdentifierCaptor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verify(logger).info(eq(INFO_CREATION_CONFIRMATION), eq(applicationIdentifierCaptor.getValue()), anyLong());
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(applicationIdentifierCaptor.getValue()), anyLong());
        order.verifyNoMoreInteractions();
        assertThat(CURRENT_SERVER.get().isStarted()).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isFalse();
        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(DEFAULT_HOST);
        assertThat(CURRENT_SERVER.get().getPort()).isNotNegative();
        assertThat(CURRENT_SERVER.get().getConfiguration()).isSameAs(applicationConfiguration);
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(workingDirectory)
            .isDirectory()
            .exists();
        assertThat(applicationIdentifierCaptor.getAllValues())
            .allMatch(value -> value.startsWith(getClass().getName()));
    }

    @Test
    void startWithWorkingDirectoryThatExists() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        applicationServer.start();

        File createdWorkingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        assertThat(isCreated).isTrue();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(givenWorkingDirectory)
            .isEqualTo(createdWorkingDirectory)
            .isDirectory()
            .exists();
    }

    @Test
    void startWithWorkingDirectoryThatDoesNotExist() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        ApplicationServer server = newApplicationServerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        Throwable actual = catchThrowable(server::start);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(givenWorkingDirectory.getAbsolutePath());
        assertThat(CURRENT_SERVER.get()).isNull();
        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void startWithWorkingDirectoryThatIsNoDirectory() throws IOException {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean fileCreated = givenWorkingDirectory.createNewFile();
        ApplicationServer server = newApplicationServerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        Throwable actual = catchThrowable(server::start);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(givenWorkingDirectory.getAbsolutePath());
        assertThat(fileCreated).isTrue();
        assertThat(CURRENT_SERVER.get()).isNull();
        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void startWithPort() {
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withPort(CUSTOM_PORT)
            .build();

        applicationServer.start();

        assertThat(CURRENT_SERVER.get().getPort()).isEqualTo(CUSTOM_PORT);
    }

    @Test
    void startWithHost() {
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withHost(CUSTOM_HOST)
            .build();

        applicationServer.start();

        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(CUSTOM_HOST);
    }

    @Test
    void startWithoutDeletingWorkingDirectoryOnShutdown() {
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withDeleteWorkingDirectoryOnShutdown(false)
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        persistentWorkingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));

        applicationServer.stop();

        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .exists()
            .isEqualTo(persistentWorkingDirectory);
    }

    @Test
    void startWithApplicationIdentifier() {
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withApplicationIdentifier(APPLICATION_IDENTIFIER)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        File workingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        ArgumentCaptor<String> captor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        order.verify(logger).info(eq(INFO_SERVER_USAGE), captor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(captor.getValue()), anyLong());
        order.verifyNoMoreInteractions();
        assertThat(captor.getValue()).isEqualTo(APPLICATION_IDENTIFIER);
        assertThat(workingDirectory.getName()).startsWith(APPLICATION_IDENTIFIER);
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(workingDirectory)
            .isDirectory()
            .exists();
    }

    @Test
    void startWithLifecycleListener() {
        StateCaptor stateCaptor = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withLifecycleListener(stateCaptor)
            .build();

        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);

        InOrder order = inOrder(stateCaptor);
        order.verify(stateCaptor).captureStarting(applicationServer);
        order.verify(stateCaptor).captureStarted(applicationServer);
        order.verifyNoMoreInteractions();
        assertThat(stateCaptor.getHalted()).isNull();
        assertThat(stateCaptor.getStarting()).isSameAs(STARTING);
        assertThat(stateCaptor.getStarted()).isSameAs(STARTING);
        assertThat(stateCaptor.getStopping()).isNull();
    }

    @Test
    void startIfAlreadyStarted() {
        StateCaptor stateCaptor = new StateCaptor();
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
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
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .build();

        ApplicationServer actual = applicationServer.stop();

        assertThat(actual).isSameAs(applicationServer);
    }

    @Test
    void stopRunningInstance() {
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .build();
        applicationServer.startInternal(new ApplicationProcessFactory(applicationServer, logger), logger);
        reset(logger);

        applicationServer.stopInternal(logger);

        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory()).doesNotExist();
        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        verify(logger).info(eq(INFO_SHUTDOWN_CONFIRMATION), eq(applicationServer.getIdentifier()), anyLong());
    }

    @Test
    void stopRunningInstanceButKeepWorkingDirectory() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .withDeleteWorkingDirectoryOnShutdown(false)
            .build()
            .start();
        reset(logger);

        applicationServer.stop();

        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        assertThat(isCreated).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(givenWorkingDirectory)
            .exists();
    }

    @Test
    void stopWithLifecycleListener() {
        StateCaptor stateCaptor = spy(new StateCaptor());
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
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
        applicationServer = newApplicationServerBuilder(applicationConfiguration)
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
    void buildWithApplicationConfigurationNotSet() {
        ApplicationServerBuilder builder = newDefaultApplicationServerBuilder();

        assertThatThrownBy(builder::build)
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("applicationConfiguration");
    }

    @Test
    void newApplicationRunnerBuilderWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> newApplicationServerBuilder(null))
            .isInstanceOf(NullPointerException.class);
    }
}
