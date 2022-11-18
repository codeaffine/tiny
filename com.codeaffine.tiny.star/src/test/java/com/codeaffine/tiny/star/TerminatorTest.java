package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationRunner.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.FilesTestHelper.fakeFileThatCannotBeDeleted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static java.util.Collections.emptyList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import com.codeaffine.tiny.star.spi.Server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class TerminatorTest {

    private static final boolean DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = true;
    private static final boolean KEEP_WORKING_DIRECTORY_ON_SHUTDOWN = false;
    private static final List<Runnable> EMPTY_PREPROCESSORS = emptyList();
    private static final String ERROR_MESSAGE = "bad";

    private boolean childOfDirectoryToDeleteHasBeenCreated;
    private PrintStream errorPrintStreamBuffer;
    private ByteArrayOutputStream errorOut;
    private File childOfDirectoryToDelete;
    private Server server;
    @TempDir
    private File workingDirectory;

    @BeforeEach
    void setUp() throws IOException {
        server = mock(Server.class);
        errorPrintStreamBuffer = System.err;
        errorOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOut));
        childOfDirectoryToDelete = new File(workingDirectory, "child");
        childOfDirectoryToDeleteHasBeenCreated = childOfDirectoryToDelete.createNewFile();
    }

    @AfterEach
    void tearDown() {
        System.setErr(errorPrintStreamBuffer);
        System.getProperties().remove(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY);
    }

    @Test
    void run() {
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        AtomicBoolean directoryExistenceOnPreprocessorExecution = new AtomicBoolean(false);
        Terminator terminator = new Terminator(workingDirectory, server, EMPTY_PREPROCESSORS, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);

        terminator.run();

        verify(server).stop();
        assertThat(directoryExistenceOnPreprocessorExecution.get()).isFalse();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void runWithShutdownPreprocessor() {
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        AtomicBoolean directoryExistenceOnPreprocessorExecution = new AtomicBoolean(false);
        Runnable preprocessor = () -> directoryExistenceOnPreprocessorExecution.set(workingDirectory.exists());
        Terminator terminator = new Terminator(workingDirectory, server, List.of(preprocessor), DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);

        terminator.run();

        verify(server).stop();
        assertThat(directoryExistenceOnPreprocessorExecution.get()).isFalse();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).exists();
        assertThat(workingDirectory).exists();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void runWithKeepWorkingDirectory() {
        Terminator terminator = new Terminator(workingDirectory, server, EMPTY_PREPROCESSORS, KEEP_WORKING_DIRECTORY_ON_SHUTDOWN);

        terminator.run();

        verify(server).stop();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).exists();
        assertThat(workingDirectory).exists();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNull();
    }

    @Test
    void runIfConstructorArgumentDirectoryToDeleteCannotBeDeleted() {
        File unDeletableFile = fakeFileThatCannotBeDeleted();
        Terminator terminator = new Terminator(unDeletableFile, server, EMPTY_PREPROCESSORS, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);

        Throwable actual = catchThrowable(terminator::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
        assertThat(errorOut.toString()).isEmpty();
    }

    @Test
    void runOnShutdownHook() {
        AtomicBoolean directoryExistenceOnPreprocessorExecution = new AtomicBoolean(false);
        Runnable preprocessor = () -> directoryExistenceOnPreprocessorExecution.set(workingDirectory.exists());
        Terminator terminator = new Terminator(workingDirectory, server, List.of(preprocessor), DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        verify(server).stop();
        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
    }

    @Test
    void runWithMultiplePreprocessorsOnShutdownHook() {
        Runnable preprocessor1 = mock(Runnable.class);
        Runnable preprocessor2 = mock(Runnable.class);
        Terminator terminator = new Terminator(workingDirectory, server, List.of(preprocessor1, preprocessor2), DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        verify(server).stop();
        InOrder order = inOrder(preprocessor1, preprocessor2);
        order.verify(preprocessor1).run();
        order.verify(preprocessor2).run();
        order.verifyNoMoreInteractions();
    }

    @Test
    void runOnShutdownHookWithErrorPronePreprocessor() {
        Runnable preprocessor1 = mock(Runnable.class);
        doThrow(new RuntimeException(ERROR_MESSAGE)).when(preprocessor1).run();
        Runnable preprocessor2 = mock(Runnable.class);
        Terminator terminator = new Terminator(workingDirectory, server, List.of(preprocessor1, preprocessor2), DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.run();

        verify(server).stop();
        assertThat(errorOut.toString()).contains(ERROR_MESSAGE);
        verify(preprocessor2).run();
    }

    @Test
    void deleteWorkingDirectory() {
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        AtomicBoolean directoryExistenceOnPreprocessorExecution = new AtomicBoolean(false);
        Runnable preprocessor = () -> directoryExistenceOnPreprocessorExecution.set(workingDirectory.exists());
        Terminator terminator = new Terminator(workingDirectory, server, List.of(preprocessor), DELETE_WORKING_DIRECTORY_ON_SHUTDOWN);
        terminator.setShutdownHookExecution(true);

        terminator.deleteWorkingDirectory();

        verify(server, never()).stop();
        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY)).isNotNull();
    }

    @Test
    void constructWithNullAsApplicationWorkingDirectoryArgument() {
        assertThatThrownBy(() -> new Terminator(null, server, EMPTY_PREPROCESSORS, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsServerArgument() {
        assertThatThrownBy(() -> new Terminator(workingDirectory, null, EMPTY_PREPROCESSORS, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsDeleteWorkingDirectoryOnProcessShutdownPreprocessorsArgument() {
        assertThatThrownBy(() -> new Terminator(workingDirectory, server, null, DELETE_WORKING_DIRECTORY_ON_SHUTDOWN))
            .isInstanceOf(NullPointerException.class);
    }
}
