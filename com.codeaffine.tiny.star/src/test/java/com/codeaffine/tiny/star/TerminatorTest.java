package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.FilesTestHelper.fakeFileThatCannotBeDeleted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.Collections.emptyList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemErrCaptor;
import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class TerminatorTest {

    private static final boolean DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = true;
    private static final boolean KEEP_WORKING_DIRECTORY_ON_SHUTDOWN = false;
    private static final List<Runnable> EMPTY_PREPROCESSORS = emptyList();
    private static final String ERROR_MESSAGE = "bad";

    private LoggingFrameworkControl loggingFrameworkControl;
    private boolean childOfDirectoryToDeleteHasBeenCreated;
    private File childOfDirectoryToDelete;
    private Server server;
    @TempDir
    private File workingDirectory;
    private Runnable shutDownHookRemover;

    @BeforeEach
    void setUp() throws IOException {
        server = mock(Server.class);
        loggingFrameworkControl = mock(LoggingFrameworkControl.class);
        childOfDirectoryToDelete = new File(workingDirectory, "child");
        childOfDirectoryToDeleteHasBeenCreated = childOfDirectoryToDelete.createNewFile();
        shutDownHookRemover = mock(Runnable.class);
    }

    @AfterEach
    void tearDown() {
        System.getProperties().remove(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY);
    }

    @Test
    void run() {
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);

        terminator.run();

        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        verify(server).stop();
        verify(loggingFrameworkControl, never()).halt();
        verify(shutDownHookRemover).run();
    }

    @Test
    void runIfLoggingFrameworkIsUsingWorkingDirectory() {
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        stubLoggingFrameworkToUseWorkingDirectory();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);

        terminator.run();

        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).exists();
        assertThat(workingDirectory).exists();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        verify(server).stop();
        verify(loggingFrameworkControl, never()).halt();
    }

    @Test
    void runWithKeepWorkingDirectory() {
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, KEEP_WORKING_DIRECTORY_ON_SHUTDOWN);

        terminator.run();

        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).exists();
        assertThat(workingDirectory).exists();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
        verify(server).stop();
        verify(loggingFrameworkControl, never()).halt();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void runIfConstructorArgumentDirectoryToDeleteCannotBeDeleted(SystemErrCaptor systemErrCaptor) {
        File unDeletableFile = fakeFileThatCannotBeDeleted();
        Terminator terminator = new Terminator(unDeletableFile, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);

        Throwable actual = catchThrowable(terminator::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
        assertThat(systemErrCaptor.getLog()).isEmpty();
    }

    @Test
    void runOnShutdownHook() {
        AtomicBoolean directoryExistenceOnPreprocessorExecution = captorWorkingDirectoryExistenceOnLoggingFrameworkControlHalt();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        InOrder order = inOrder(server, loggingFrameworkControl);
        order.verify(server).stop();
        order.verify(loggingFrameworkControl).halt();
        verify(loggingFrameworkControl, never()).isUsingWorkingDirectory();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void runOnShutdownHookWithLoggingFrameworkControlErrorOnHalt(SystemErrCaptor systemErrCaptor) {
        stubLoggingFrameworkControlWithErrorOnHalt();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        assertThat(systemErrCaptor.getLog()).contains(ERROR_MESSAGE);
        verify(server).stop();
    }

    @Test
    void deleteWorkingDirectory() {
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        AtomicBoolean directoryExistenceOnPreprocessorExecution = captorWorkingDirectoryExistenceOnLoggingFrameworkControlHalt();
        Terminator terminator = new Terminator(workingDirectory, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.deleteWorkingDirectory();

        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNotNull();
        verify(server, never()).stop();
    }

    @Test
    void constructWithNullAsApplicationWorkingDirectoryArgument() {
        assertThatThrownBy(() -> new Terminator(null, server, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsServerArgument() {
        assertThatThrownBy(() -> new Terminator(workingDirectory, null, loggingFrameworkControl, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsDeleteWorkingDirectoryOnProcessShutdownPreprocessorsArgument() {
        assertThatThrownBy(() -> new Terminator(workingDirectory, server, null, shutDownHookRemover, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN))
            .isInstanceOf(NullPointerException.class);
    }

    private void stubLoggingFrameworkToUseWorkingDirectory() {
        when(loggingFrameworkControl.isUsingWorkingDirectory()).thenReturn(true);
    }

    private void stubLoggingFrameworkControlWithErrorOnHalt() {
        doThrow(new RuntimeException(ERROR_MESSAGE)).when(loggingFrameworkControl).halt();
    }

    private AtomicBoolean captorWorkingDirectoryExistenceOnLoggingFrameworkControlHalt() {
        AtomicBoolean result = new AtomicBoolean(false);
        doAnswer(invocation -> captureWorkingDirectoryExistance(result))
            .when(loggingFrameworkControl)
            .halt();
        return result;
    }

    private Void captureWorkingDirectoryExistance(AtomicBoolean workingDirectoryExistenceValue) {
        workingDirectoryExistenceValue.set(workingDirectory.exists());
        return null;
    }
}
