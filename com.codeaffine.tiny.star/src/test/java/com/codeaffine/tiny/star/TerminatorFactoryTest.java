package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;

class TerminatorFactoryTest {

    private static final LoggingFrameworkControl LOGGING_FRAMEWORK_CONTROL = mock(LoggingFrameworkControl.class);
    private static final File WORKING_DIRECTORY = new File("dir");
    private static final Server SERVER = mock(Server.class);

    private Runnable shutdownHookRemover;
    private TerminatorFactory factory;

    @BeforeEach
    void setUp() {
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {})
            .withDeleteWorkingDirectoryOnShutdown(true)
            .build();
        factory = new TerminatorFactory(applicationServer);
        shutdownHookRemover = mock(Runnable.class);
    }

    @Test
    void create() {
        Terminator actual = factory.create(WORKING_DIRECTORY, SERVER, LOGGING_FRAMEWORK_CONTROL, shutdownHookRemover);

        assertThat(actual).isNotNull();
    }

    @Test
    void createWithNullAsWorkingDirectoryArgument() {
        assertThatThrownBy(() -> factory.create(null, SERVER, LOGGING_FRAMEWORK_CONTROL, shutdownHookRemover))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createWithNullAsServerArgument() {
        assertThatThrownBy(() -> factory.create(WORKING_DIRECTORY, null, LOGGING_FRAMEWORK_CONTROL, shutdownHookRemover))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createWithNullAsLoggingFrameworkControlArgument() {
        assertThatThrownBy(() -> factory.create(WORKING_DIRECTORY, SERVER, null, shutdownHookRemover))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createWithNullAsShutdownHookRemoverArgument() {
        assertThatThrownBy(() -> factory.create(WORKING_DIRECTORY, SERVER, LOGGING_FRAMEWORK_CONTROL, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationServerArgument() {
        assertThatThrownBy(() -> new TerminatorFactory(null))
            .isInstanceOf(NullPointerException.class);
    }
}
