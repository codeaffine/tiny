package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ShutdownHookRemoverTest {

    private LoggingFrameworkControl loggingFrameworkControl;
    private ShutdownHookHandler shutdownHookHandler;
    private Runnable shutdownHookOperation;
    private AtomicReference<Runnable> shutdownHookOperationHolder;

    @BeforeEach
    void setUp() {
        loggingFrameworkControl = mock(LoggingFrameworkControl.class);
        shutdownHookHandler = mock(ShutdownHookHandler.class);
        shutdownHookOperation = mock(Runnable.class);
        shutdownHookOperationHolder = new AtomicReference<>(shutdownHookOperation);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, 0",
        "true, false, 1",
        "false, true, 1",
        "false, false, 1"
    })
    void run(boolean workingDirectoryInUseByLoggingFramework, boolean deleteWorkingDirectoryOnShutdown, int expectedNumberOfInvocations) {
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {})
            .withDeleteWorkingDirectoryOnShutdown(deleteWorkingDirectoryOnShutdown)
            .build();
        stubLoggingFrameworkControlIsUsingWorkingDirectory(workingDirectoryInUseByLoggingFramework);
        ShutdownHookRemover shutdownHookRemover = new ShutdownHookRemover(
            applicationServer,
            loggingFrameworkControl,
            shutdownHookHandler,
            shutdownHookOperationHolder);

        shutdownHookRemover.run();

        verify(shutdownHookHandler, times(expectedNumberOfInvocations)).deregister(shutdownHookOperation);
    }

    @Test
    void constructWithNullAsApplicationServerArgument() {
        assertThatThrownBy(() -> new ShutdownHookRemover(null, loggingFrameworkControl, shutdownHookHandler, shutdownHookOperationHolder))
            .isInstanceOf(NullPointerException.class );
    }

    @Test
    void constructWithNullAsLoggingFrameworkControlArgument() {
        assertThatThrownBy(() -> new ShutdownHookRemover(mock(ApplicationServer.class), null, shutdownHookHandler, shutdownHookOperationHolder))
            .isInstanceOf(NullPointerException.class );
    }

    @Test
    void constructWithNullAsShutdownHookHandlerArgument() {
        assertThatThrownBy(() -> new ShutdownHookRemover(mock(ApplicationServer.class), loggingFrameworkControl, null, shutdownHookOperationHolder))
            .isInstanceOf(NullPointerException.class );
    }

    @Test
    void constructWithNullAsShutdownHookOperationHolderArgument() {
        assertThatThrownBy(() -> new ShutdownHookRemover(mock(ApplicationServer.class), loggingFrameworkControl, shutdownHookHandler, null))
            .isInstanceOf(NullPointerException.class );
    }

    private void stubLoggingFrameworkControlIsUsingWorkingDirectory(boolean workingDirectoryInUseByLoggingFramework) {
        when(loggingFrameworkControl.isUsingWorkingDirectory()).thenReturn(workingDirectoryInUseByLoggingFramework);
    }
}
