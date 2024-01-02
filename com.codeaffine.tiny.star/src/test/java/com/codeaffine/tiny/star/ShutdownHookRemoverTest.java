/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
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
        ApplicationServer.ApplicationServerBuilder applicationServerBuilder = newApplicationServerBuilder(application -> {});
        if(!deleteWorkingDirectoryOnShutdown) {
            applicationServerBuilder.keepWorkingDirectoryOnShutdown();
        }
        ApplicationServer applicationServer = applicationServerBuilder.build();
        stubLoggingFrameworkControlIsBlockingWorkingDirectory(workingDirectoryInUseByLoggingFramework);
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

    private void stubLoggingFrameworkControlIsBlockingWorkingDirectory(boolean workingDirectoryInUseByLoggingFramework) {
        when(loggingFrameworkControl.isBlockingWorkingDirectory()).thenReturn(workingDirectoryInUseByLoggingFramework);
    }
}
