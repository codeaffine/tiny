package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationRunner.DEFAULT_HOST;
import static com.codeaffine.tiny.star.ApplicationRunner.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.ApplicationRunner.newApplicationRunnerBuilder;
import static com.codeaffine.tiny.star.ApplicationRunnerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.star.Messages.INFO_SERVER_USAGE;
import static com.codeaffine.tiny.star.Messages.INFO_STARTUP_CONFIRMATION;
import static com.codeaffine.tiny.star.Messages.INFO_WORKING_DIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.slf4j.Logger;

import java.io.File;

@ExtendWith(ApplicationRunnerTestContext.class)
class ApplicationRunnerTest implements ApplicationConfiguration {

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
    }

    @Test
    void run() {
        ApplicationInstance applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
            .withLogger(logger)
            .build()
            .run();

        File workikingDirectory = new File(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        assertThat(CURRENT_SERVER.get().isStarted()).isTrue();
        assertThat(CURRENT_SERVER.get().isStopped()).isFalse();
        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(DEFAULT_HOST);
        assertThat(CURRENT_SERVER.get().getPort()).isNotNegative();
        assertThat(CURRENT_SERVER.get().getWorkingDirectory()).isEqualTo(workikingDirectory);
        assertThat(CURRENT_SERVER.get().getConfiguration()).isSameAs(this);
        assertThat(workikingDirectory).exists();
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_SERVER_USAGE, getClass().getName(), CURRENT_SERVER.get().getName());
        order.verify(logger).info(INFO_WORKING_DIRECTORY, System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));
        order.verify(logger).info(eq(INFO_STARTUP_CONFIRMATION), eq(getClass().getName()), anyLong());
        order.verifyNoMoreInteractions();
    }

    @Test
    void stop() {
        ApplicationInstance applicationInstance = newApplicationRunnerBuilder()
            .withApplicationConfiguration(this)
            .withLogger(logger)
            .build()
            .run();
        reset(logger);

        applicationInstance.stop();

        assertThat(CURRENT_SERVER.get().isStopped()).isTrue();
    }

    @Override
    public void configure(Application application) {

    }
}
