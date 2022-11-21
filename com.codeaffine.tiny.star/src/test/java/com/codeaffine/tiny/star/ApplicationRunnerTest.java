package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationRunner.*;
import static com.codeaffine.tiny.star.ApplicationRunner.DEFAULT_HOST;
import static com.codeaffine.tiny.star.ApplicationRunner.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.ApplicationRunner.newApplicationRunnerBuilder;
import static com.codeaffine.tiny.star.ApplicationRunnerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.star.Messages.INFO_SERVER_USAGE;
import static com.codeaffine.tiny.star.Messages.INFO_STARTUP_CONFIRMATION;
import static com.codeaffine.tiny.star.Messages.INFO_WORKING_DIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import static java.lang.System.getProperty;
import static java.util.Objects.nonNull;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

@ExtendWith(ApplicationRunnerTestContext.class)
class ApplicationRunnerTest implements ApplicationConfiguration {

    private static final int CUSTOM_PORT = Integer.MAX_VALUE;
    private static final String CUSTOM_HOST = "host";

    private ApplicationInstance applicationInstance;
    private Logger logger;
    @TempDir
    private File tempDir;

    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
    }

    @AfterEach
    void tearDown() {
        if(nonNull(applicationInstance)) {
            applicationInstance.stop();
        }
    }

    @Test
    void run() {
        applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
            .withLogger(logger)
            .build()
            .run();

        File workingDirectory = new File(getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        assertThat(CURRENT_SERVER.get().isStarted()).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isFalse();
        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(DEFAULT_HOST);
        assertThat(CURRENT_SERVER.get().getPort()).isNotNegative();
        assertThat(CURRENT_SERVER.get().getConfiguration()).isSameAs(this);
        assertThat(CURRENT_SERVER.get().getWorkingDirectory())
            .isEqualTo(workingDirectory)
            .isDirectory()
            .exists();
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_SERVER_USAGE, getClass().getName(), CURRENT_SERVER.get().getName());
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(getClass().getName()), anyLong());
        order.verifyNoMoreInteractions();
    }

    @Test
    void runWithGivenWorkingDirectoryThatExists() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean isCreated = givenWorkingDirectory.mkdirs();
        applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
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
    void runWithGivenWorkingDirectoryThatDoesNotExist() {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        ApplicationRunner runner = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
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
    void runWithGivenWorkingDirectoryThatIsNoDirectory() throws IOException {
        File givenWorkingDirectory = new File(tempDir, "workingDirectory");
        boolean fileCreated = givenWorkingDirectory.createNewFile();
        ApplicationRunner runner = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
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
        applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
            .withPort(CUSTOM_PORT)
            .build()
            .run();

        assertThat(CURRENT_SERVER.get().getPort()).isEqualTo(CUSTOM_PORT);
    }

    @Test
    void runWithHost() {
        applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
            .withHost(CUSTOM_HOST)
            .build()
            .run();

        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(CUSTOM_HOST);
    }
    @Test
    void stopRunningInstance() {
        applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
            .withLogger(logger)
            .build()
            .run();
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
        applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
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
    void buildWithNullAsApplicationConfigurationArgument() {
        ApplicationRunnerBuilder builder = newApplicationRunnerBuilder();

        assertThatThrownBy(builder::build)
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("applicationConfiguration");
    }

    @Override
    public void configure(Application application) {

    }
}
