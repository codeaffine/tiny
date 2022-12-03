package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationRunner.*;
import static com.codeaffine.tiny.star.ApplicationRunner.DEFAULT_HOST;
import static com.codeaffine.tiny.star.ApplicationRunner.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.ApplicationRunner.newApplicationRunnerBuilder;
import static com.codeaffine.tiny.star.ApplicationRunnerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.star.IoUtils.deleteDirectory;
import static com.codeaffine.tiny.star.Texts.INFO_SERVER_USAGE;
import static com.codeaffine.tiny.star.Texts.INFO_STARTUP_CONFIRMATION;
import static com.codeaffine.tiny.star.Texts.INFO_WORKING_DIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import static java.lang.System.getProperty;
import static java.util.Objects.nonNull;

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

@ExtendWith(ApplicationRunnerTestContext.class)
class ApplicationRunnerTest {

    private static final String APPLICATION_IDENTIFIER = "applicationIdentifier";
    private static final int CUSTOM_PORT = Integer.MAX_VALUE;
    private static final String CUSTOM_HOST = "host";

    private ApplicationConfiguration applicationConfiguration;
    private ApplicationInstance applicationInstance;
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
        if(nonNull(applicationInstance)) {
            applicationInstance.stop();
        }
        if(nonNull(persistentWorkingDirectory) && persistentWorkingDirectory.exists()) {
            deleteDirectory(persistentWorkingDirectory);
        }
    }

    @Test
    void run() {
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .build()
            .runInternal(logger);

        File workingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        ArgumentCaptor<String> captor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(eq(INFO_SERVER_USAGE), captor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(captor.getValue()), anyLong());
        order.verifyNoMoreInteractions();
        assertThat(captor.getValue()).startsWith(getClass().getName());
        assertThat(CURRENT_SERVER.get().isStarted()).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isFalse();
        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(DEFAULT_HOST);
        assertThat(CURRENT_SERVER.get().getPort()).isNotNegative();
        assertThat(CURRENT_SERVER.get().getConfiguration()).isSameAs(applicationConfiguration);
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(workingDirectory)
            .isDirectory()
            .exists();
    }

    @Test
    void runWithWorkingDirectoryThatExists() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .build()
            .run();

        File createdWorkingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        assertThat(isCreated).isTrue();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(givenWorkingDirectory)
            .isEqualTo(createdWorkingDirectory)
            .isDirectory()
            .exists();
    }

    @Test
    void runWithWorkingDirectoryThatDoesNotExist() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        ApplicationRunner runner = newApplicationRunnerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        Throwable actual = catchThrowable(runner::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(givenWorkingDirectory.getAbsolutePath());
        assertThat(CURRENT_SERVER.get()).isNull();
        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void runWithWorkingDirectoryThatIsNoDirectory() throws IOException {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean fileCreated = givenWorkingDirectory.createNewFile();
        ApplicationRunner runner = newApplicationRunnerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .build();

        Throwable actual = catchThrowable(runner::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(givenWorkingDirectory.getAbsolutePath());
        assertThat(fileCreated).isTrue();
        assertThat(CURRENT_SERVER.get()).isNull();
        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void runWithPort() {
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .withPort(CUSTOM_PORT)
            .build()
            .run();

        assertThat(CURRENT_SERVER.get().getPort()).isEqualTo(CUSTOM_PORT);
    }

    @Test
    void runWithHost() {
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .withHost(CUSTOM_HOST)
            .build()
            .run();

        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(CUSTOM_HOST);
    }

    @Test
    void runWithoutDeletingWorkingDirectoryOnShutdown() {
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .withDeleteWorkingDirectoryOnShutdown(false)
            .build()
            .runInternal(logger);
        persistentWorkingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));

        applicationInstance.stop();

        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .exists()
            .isEqualTo(persistentWorkingDirectory);
    }

    @Test
    void runWithApplicationIdentifier() {
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .withApplicationIdentifier(APPLICATION_IDENTIFIER)
            .build()
            .runInternal(logger);

        File workingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        ArgumentCaptor<String> captor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(eq(INFO_SERVER_USAGE), captor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
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
    void stopRunningInstance() {
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .build()
            .runInternal(logger);
        reset(logger);

        applicationInstance.stop();

        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory()).doesNotExist();
        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void stopRunningInstanceButKeepWorkingDirectory() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationInstance = newApplicationRunnerBuilder(applicationConfiguration)
            .withWorkingDirectory(givenWorkingDirectory)
            .withDeleteWorkingDirectoryOnShutdown(false)
            .build()
            .run();
        reset(logger);

        applicationInstance.stop();

        assertThat(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        assertThat(isCreated).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(givenWorkingDirectory)
            .exists();
    }

    @Test
    void buildWithApplicationConfigurationNotSet() {
        ApplicationRunnerBuilder builder = newDefaultApplicationRunnerBuilder();

        assertThatThrownBy(builder::build)
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("applicationConfiguration");
    }

    @Test
    void newApplicationRunnerBuilderWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> newApplicationRunnerBuilder(null))
            .isInstanceOf(NullPointerException.class);
    }
}
