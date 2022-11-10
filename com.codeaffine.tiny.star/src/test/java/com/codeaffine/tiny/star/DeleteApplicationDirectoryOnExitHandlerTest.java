package com.codeaffine.tiny.star;

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
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class DeleteApplicationDirectoryOnExitHandlerTest {

    private static final String UNKNOWN_LOG_MANAGER_CLASS = "unknownLogManagerClass";
    private static final String SHUTDOWN_METHOD = "shutdown";
    private static final String WRONG_METHOD = "wrongMethod";
    private static final String ERROR_MESSAGE = "bad";

    private PrintStream errorPrintStreamBuffer;
    private File childOfDirectoryToDelete;
    private boolean fileHasBeenCreated;
    private ByteArrayOutputStream errorOut;
    @TempDir
    private File directoryToDelete;

    static class FakeLogManager {

        static final AtomicReference<Exception> problemHolder = new AtomicReference<>();
        static final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

        static void initialize() {
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
        FakeLogManager.initialize();
        childOfDirectoryToDelete = new File(directoryToDelete, "test");
        fileHasBeenCreated = childOfDirectoryToDelete.createNewFile();
    }

    @AfterEach
    void tearDown() {
        System.setErr(errorPrintStreamBuffer);
    }

    @Test
    void run() {
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(directoryToDelete.toPath(), FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isTrue();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
    }

    @Test
    void runIfLog4JIsNotUsed() {
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(directoryToDelete.toPath(), UNKNOWN_LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
    }

    @Test
    void runIfShutdownMethodIsNotFoundOrUnaccessible() {
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(directoryToDelete.toPath(), FakeLogManager.class.getName(), WRONG_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(errorOut.toString())
            .contains(FakeLogManager.class.getName())
            .contains(WRONG_METHOD);
    }

    @Test
    void runIfShutdownMethodThrowsRuntimeException() {
        FakeLogManager.problemHolder.set(new RuntimeException(ERROR_MESSAGE));
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(directoryToDelete.toPath(), FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(errorOut.toString())
            .contains(FakeLogManager.class.getName())
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void runIfShutdownMethodThrowsException() {
        FakeLogManager.problemHolder.set(new Exception(ERROR_MESSAGE));
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(directoryToDelete.toPath(), FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(errorOut.toString())
            .contains(FakeLogManager.class.getName())
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void runIfDirectoryCannotBeDeleted() {
        Path unknownDirectoryPath = new File("unknown").toPath();
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(unknownDirectoryPath, FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        Throwable actual = catchThrowable(shutdownHook::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unknownDirectoryPath.toString());
        assertThat(errorOut.toString()).isEmpty();
    }

    @Test
    void runIfChildOfRootDirectoryCannotBeDeleted() {
        Path unknownDirectoryPath = new File(directoryToDelete, "unknown").toPath();
        DeleteApplicationDirectoryOnExitHandler shutdownHook = new DeleteApplicationDirectoryOnExitHandler(unknownDirectoryPath, FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        Throwable actual = catchThrowable(shutdownHook::run);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unknownDirectoryPath.toString());
        assertThat(errorOut.toString()).isEmpty();
    }

    @Test
    void constructWithNullAsPathArgument() {
        String logManagerClass = FakeLogManager.class.getName();

        assertThatThrownBy(() -> new DeleteApplicationDirectoryOnExitHandler(null, logManagerClass, SHUTDOWN_METHOD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsLogManagerClassNameArgument() {
        Path path = directoryToDelete.toPath();

        assertThatThrownBy(() -> new DeleteApplicationDirectoryOnExitHandler(path, null, SHUTDOWN_METHOD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsShutdownMethodNameArgument() {
        String logManagerClass = FakeLogManager.class.getName();
        Path path = directoryToDelete.toPath();

        assertThatThrownBy(() -> new DeleteApplicationDirectoryOnExitHandler(path, logManagerClass, null))
            .isInstanceOf(NullPointerException.class);
    }
}
