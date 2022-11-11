package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.FilesTestHelper.fakeFileThatCannotBeDeleted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static java.util.Collections.emptyList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class DeleteWorkingDirectoryOnProcessShutdownHandlerTest {

    private static final String ERROR_MESSAGE = "bad";

    private boolean childOfDirectoryToDeleteHasBeenCreated;
    private PrintStream errorPrintStreamBuffer;
    private ByteArrayOutputStream errorOut;
    private File childOfDirectoryToDelete;
    @TempDir
    private File workingDirectory;

    @BeforeEach
    void setUp() throws IOException {
        errorPrintStreamBuffer = System.err;
        errorOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOut));
        childOfDirectoryToDelete = new File(workingDirectory, "child");
        childOfDirectoryToDeleteHasBeenCreated = childOfDirectoryToDelete.createNewFile();
    }

    @AfterEach
    void tearDown() {
        System.setErr(errorPrintStreamBuffer);
    }

    @Test
    void run() {
        AtomicBoolean directoryExistenceOnPreprocessorExecution = new AtomicBoolean(false);
        Runnable preprocessor = () -> directoryExistenceOnPreprocessorExecution.set(workingDirectory.exists());
        DeleteWorkingDirectoryOnProcessShutdownHandler handler = new DeleteWorkingDirectoryOnProcessShutdownHandler(workingDirectory, List.of(preprocessor));

        handler.run();

        assertThat(directoryExistenceOnPreprocessorExecution.get()).isTrue();
        assertThat(childOfDirectoryToDeleteHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
    }

    @Test
    void runWithMultiplePreprocessors() {
        Runnable preprocessor1 = mock(Runnable.class);
        Runnable preprocessor2 = mock(Runnable.class);
        List<Runnable> preprocessors = List.of(preprocessor1, preprocessor2);
        DeleteWorkingDirectoryOnProcessShutdownHandler handler = new DeleteWorkingDirectoryOnProcessShutdownHandler(workingDirectory, preprocessors);

        handler.run();

        InOrder order = inOrder(preprocessor1, preprocessor2);
        order.verify(preprocessor1).run();
        order.verify(preprocessor2).run();
        order.verifyNoMoreInteractions();
    }

    @Test
    void runWithErrorPronePreprocessor() {
        Runnable preprocessor1 = mock(Runnable.class);
        doThrow(new RuntimeException(ERROR_MESSAGE)).when(preprocessor1).run();
        Runnable preprocessor2 = mock(Runnable.class);
        List<Runnable> preprocessors = List.of(preprocessor1, preprocessor2);
        DeleteWorkingDirectoryOnProcessShutdownHandler handler = new DeleteWorkingDirectoryOnProcessShutdownHandler(workingDirectory, preprocessors);

        handler.run();

        assertThat(errorOut.toString()).contains(ERROR_MESSAGE);
        verify(preprocessor2).run();
    }

    @Test
    void runIfConstructorArgumentDirectoryToDeleteCannotBeDeleted() {
        File unDeletableFile = fakeFileThatCannotBeDeleted();
        DeleteWorkingDirectoryOnProcessShutdownHandler handler = new DeleteWorkingDirectoryOnProcessShutdownHandler(unDeletableFile, emptyList());

        Throwable actual = catchThrowable(handler::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
        assertThat(errorOut.toString()).isEmpty();
    }

    @Test
    void constructWithNullAsPathArgument() {
        List<Runnable> emptyPreprocessors = emptyList();

        assertThatThrownBy(() -> new DeleteWorkingDirectoryOnProcessShutdownHandler(null, emptyPreprocessors))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsDeleteWorkingDirectoryOnProcessShutdownPreprocessorsArgument() {
        assertThatThrownBy(() -> new DeleteWorkingDirectoryOnProcessShutdownHandler(workingDirectory, null))
            .isInstanceOf(NullPointerException.class);
    }
}
