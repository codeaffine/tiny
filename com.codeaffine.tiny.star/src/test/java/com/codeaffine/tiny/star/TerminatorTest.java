/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.test.SystemPrintStreamCaptor.SystemErrCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codeaffine.tiny.star.FilesTestHelper.fakeFileThatCannotBeDeleted;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminatorTest {

    private static final String ERROR_MESSAGE = "bad";

    private LoggingFrameworkControl loggingFrameworkControl;
    private boolean childOfDirectoryToDeleteHasBeenCreated;
    private ApplicationServer applicationServer;
    private File childOfDirectoryToDelete;
    private Runnable shutDownHookRemover;
    private Server server;
    @TempDir
    private File workingDirectory;

    @BeforeEach
    void setUp() throws IOException {
        server = mock(Server.class);
        loggingFrameworkControl = mock(LoggingFrameworkControl.class);
        childOfDirectoryToDelete = new File(workingDirectory, "child");
        childOfDirectoryToDeleteHasBeenCreated = childOfDirectoryToDelete.createNewFile();
        shutDownHookRemover = mock(Runnable.class);
        applicationServer = stubApplicationServer();
    }

    @AfterEach
    void tearDown() {
        System.getProperties().remove(applicationServer.getWorkingDirectorSystemProperty());
    }

    @Test
    void run() {
        System.setProperty(applicationServer.getWorkingDirectorSystemProperty(), workingDirectory.getAbsolutePath());
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);

        terminator.run();

        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(System.getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNull();
        verify(server).stop();
        verify(loggingFrameworkControl, never()).halt();
        verify(shutDownHookRemover).run();
    }

    @Test
    void runIfLoggingFrameworkIsBlockingWorkingDirectory() {
        System.setProperty(applicationServer.getWorkingDirectorSystemProperty(), workingDirectory.getAbsolutePath());
        stubLoggingFrameworkToUseWorkingDirectory();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);

        terminator.run();

        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).exists();
        assertThat(workingDirectory).exists();
        assertThat(System.getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNull();
        verify(server).stop();
        verify(loggingFrameworkControl, never()).halt();
    }

    @Test
    void runWithKeepWorkingDirectory() {
        applicationServer.deleteWorkingDirectoryOnShutdown = false;
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);

        terminator.run();

        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).exists();
        assertThat(workingDirectory).exists();
        assertThat(System.getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNull();
        verify(server).stop();
        verify(loggingFrameworkControl, never()).halt();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void runIfConstructorArgumentDirectoryToDeleteCannotBeDeleted(SystemErrCaptor systemErrCaptor) {
        File unDeletableFile = fakeFileThatCannotBeDeleted();
        Terminator terminator = new Terminator(unDeletableFile, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);

        Exception actual = catchException(terminator::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
        assertThat(systemErrCaptor.getLog()).isEmpty();
    }

    @Test
    void runOnShutdownHook() {
        AtomicBoolean directoryExistenceOnPreprocessorExecution = captorWorkingDirectoryExistenceOnLoggingFrameworkControlHalt();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        InOrder order = inOrder(server, loggingFrameworkControl);
        order.verify(server).stop();
        order.verify(loggingFrameworkControl).halt();
        verify(loggingFrameworkControl, never()).isBlockingWorkingDirectory();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void runOnShutdownHookWithLoggingFrameworkControlErrorOnHalt(SystemErrCaptor systemErrCaptor) {
        stubLoggingFrameworkControlWithErrorOnHalt();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        assertThat(systemErrCaptor.getLog()).contains(ERROR_MESSAGE);
        verify(server).stop();
    }

    @Test
    void deleteWorkingDirectory() {
        System.setProperty(applicationServer.getWorkingDirectorSystemProperty(), workingDirectory.getAbsolutePath());
        AtomicBoolean directoryExistenceOnPreprocessorExecution = captorWorkingDirectoryExistenceOnLoggingFrameworkControlHalt();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, applicationServer);
        terminator.setShutdownHookExecution(true);

        terminator.deleteWorkingDirectory();

        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(System.getProperty(applicationServer.getWorkingDirectorSystemProperty())).isNotNull();
        verify(server, never()).stop();
    }

    @Test
    void constructWithNullAsApplicationWorkingDirectoryArgument() {
        assertThatThrownBy(() -> new Terminator(null, server, loggingFrameworkControl, shutDownHookRemover, applicationServer))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsServerArgument() {
        assertThatThrownBy(() -> new Terminator(workingDirectory, null, loggingFrameworkControl, shutDownHookRemover, applicationServer))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsDeleteWorkingDirectoryOnProcessShutdownPreprocessorsArgument() {
        assertThatThrownBy(() -> new Terminator(workingDirectory, server, null, shutDownHookRemover, applicationServer))
            .isInstanceOf(NullPointerException.class);
    }

    private void stubLoggingFrameworkToUseWorkingDirectory() {
        when(loggingFrameworkControl.isBlockingWorkingDirectory()).thenReturn(true);
    }

    private void stubLoggingFrameworkControlWithErrorOnHalt() {
        doThrow(new RuntimeException(ERROR_MESSAGE)).when(loggingFrameworkControl).halt();
    }

    private AtomicBoolean captorWorkingDirectoryExistenceOnLoggingFrameworkControlHalt() {
        AtomicBoolean result = new AtomicBoolean(false);
        doAnswer(invocation -> captureWorkingDirectoryExistence(result))
            .when(loggingFrameworkControl)
            .halt();
        return result;
    }

    private Void captureWorkingDirectoryExistence(AtomicBoolean workingDirectoryExistenceValue) {
        workingDirectoryExistenceValue.set(workingDirectory.exists());
        return null;
    }

    private static ApplicationServer stubApplicationServer() {
        ApplicationServer result = mock(ApplicationServer.class);
        when(result.getWorkingDirectorSystemProperty()).thenReturn("workingDirectorySystemProperty");
        result.deleteWorkingDirectoryOnShutdown = true;
        return result;
    }
}
