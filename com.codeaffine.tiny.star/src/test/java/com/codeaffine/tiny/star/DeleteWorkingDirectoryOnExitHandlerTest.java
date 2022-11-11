package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.DeleteWorkingDirectoryOnExitHandlerTest.FakeLogManager.initializeFakeLogManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import static java.util.Objects.nonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class DeleteWorkingDirectoryOnExitHandlerTest {

    private static final String UNKNOWN_LOG_MANAGER_CLASS = "unknownLogManagerClass";
    private static final String LOG_MANAGER_CLASS = FakeLogManager.class.getName();
    private static final String SHUTDOWN_METHOD = "shutdown";
    private static final String WRONG_METHOD = "wrongMethod";
    private static final String ERROR_MESSAGE = "bad";

    private PrintStream errorPrintStreamBuffer;
    private File childOfDirectoryToDelete;
    private boolean fileHasBeenCreated;
    private ByteArrayOutputStream errorOut;
    @TempDir
    private File workingDirectory;

    static class FakeLogManager {

        static final AtomicReference<Exception> problemHolder = new AtomicReference<>();
        static final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

        static void initializeFakeLogManager() {
            shutdownCalled.set(false);
            problemHolder.set(null);
        }

        public static void shutdown() throws Exception {
            if(nonNull(problemHolder.get())) {
                throw problemHolder.get();
            }
            shutdownCalled.set(true);
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        errorPrintStreamBuffer = System.err;
        errorOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOut));
        initializeFakeLogManager();
        childOfDirectoryToDelete = new File(workingDirectory, "child");
        fileHasBeenCreated = childOfDirectoryToDelete.createNewFile();
    }

    @AfterEach
    void tearDown() {
        System.setErr(errorPrintStreamBuffer);
    }

    @Test
    void run() {
        DeleteWorkingDirectoryOnExitHandler handler = new DeleteWorkingDirectoryOnExitHandler(workingDirectory, LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isTrue();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
    }

    @Test
    void runIfLog4JIsNotUsed() {
        DeleteWorkingDirectoryOnExitHandler handler = new DeleteWorkingDirectoryOnExitHandler(workingDirectory, UNKNOWN_LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
    }

    @Test
    void runIfShutdownMethodIsNotFoundOrUnaccessible() {
        DeleteWorkingDirectoryOnExitHandler handler = new DeleteWorkingDirectoryOnExitHandler(workingDirectory, LOG_MANAGER_CLASS, WRONG_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(errorOut.toString())
            .contains(LOG_MANAGER_CLASS)
            .contains(WRONG_METHOD);
    }

    @Test
    void runIfShutdownMethodThrowsRuntimeException() {
        FakeLogManager.problemHolder.set(new RuntimeException(ERROR_MESSAGE));
        DeleteWorkingDirectoryOnExitHandler handler = new DeleteWorkingDirectoryOnExitHandler(workingDirectory, LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(errorOut.toString())
            .contains(LOG_MANAGER_CLASS)
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void runIfShutdownMethodThrowsException() {
        FakeLogManager.problemHolder.set(new Exception(ERROR_MESSAGE));
        DeleteWorkingDirectoryOnExitHandler handler = new DeleteWorkingDirectoryOnExitHandler(workingDirectory, LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(workingDirectory).doesNotExist();
        assertThat(errorOut.toString())
            .contains(LOG_MANAGER_CLASS)
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void runIfConstructorArgumentDirectoryToDeleteCannotBeDeleted() {
        File unDeletableFile = fakeFileThatCannotBeDeleted();
        DeleteWorkingDirectoryOnExitHandler handler = new DeleteWorkingDirectoryOnExitHandler(unDeletableFile, LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        Throwable actual = catchThrowable(handler::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
        assertThat(errorOut.toString()).isEmpty();
    }

    @Test
    void constructWithNullAsPathArgument() {
        assertThatThrownBy(() -> new DeleteWorkingDirectoryOnExitHandler(null, LOG_MANAGER_CLASS, SHUTDOWN_METHOD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsLogManagerClassNameArgument() {
        assertThatThrownBy(() -> new DeleteWorkingDirectoryOnExitHandler(workingDirectory, null, SHUTDOWN_METHOD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsShutdownMethodNameArgument() {
        assertThatThrownBy(() -> new DeleteWorkingDirectoryOnExitHandler(workingDirectory, LOG_MANAGER_CLASS, null))
            .isInstanceOf(NullPointerException.class);
    }

    private static File fakeFileThatCannotBeDeleted() {
        return new File("unknown") {
            @Override
            public boolean exists() {
                return true;
            }
        };
    }
}
