package com.codeaffine.tiny.star;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

class DeleteOnExitShutdownHookTest {

    private static final String UNKNOWN_LOG_MANAGER_CLASS = "unknownLogManagerClass";
    private static final String SHUTDOWN_METHOD = "shutdown";
    private static final String WRONG_METHOD = "wrongMethod";
    private static final String ERROR_MESSAGE = "bad";

    private PrintStream errorPrintStreamBuffer;
    private File childOfDirectoryToDelete;
    private boolean fileHasBeenCreated;
    private ByteArrayOutputStream out;
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
        out = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(out);
        System.setErr(err);
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
        DeleteOnExitShutdownHook shutdownHook = new DeleteOnExitShutdownHook(directoryToDelete.toPath(), FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isTrue();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
    }

    @Test
    void runIfLog4JIsNotUsed() {
        DeleteOnExitShutdownHook shutdownHook = new DeleteOnExitShutdownHook(directoryToDelete.toPath(), UNKNOWN_LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
    }

    @Test
    void runIfShutdownMethodIsNotFoundOrUnaccessible() {
        DeleteOnExitShutdownHook shutdownHook = new DeleteOnExitShutdownHook(directoryToDelete.toPath(), FakeLogManager.class.getName(), WRONG_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(out.toString())
            .contains(FakeLogManager.class.getName())
            .contains(WRONG_METHOD);
    }

    @Test
    void runIfShutdownMethodThrowsRuntimeException() {
        FakeLogManager.problemHolder.set(new RuntimeException(ERROR_MESSAGE));
        DeleteOnExitShutdownHook shutdownHook = new DeleteOnExitShutdownHook(directoryToDelete.toPath(), FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(out.toString())
            .contains(FakeLogManager.class.getName())
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void runIfShutdownMethodThrowsException() {
        FakeLogManager.problemHolder.set(new Exception(ERROR_MESSAGE));
        DeleteOnExitShutdownHook shutdownHook = new DeleteOnExitShutdownHook(directoryToDelete.toPath(), FakeLogManager.class.getName(), SHUTDOWN_METHOD);

        shutdownHook.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(fileHasBeenCreated).isTrue();
        assertThat(childOfDirectoryToDelete).doesNotExist();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(out.toString())
            .contains(FakeLogManager.class.getName())
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void constructWithNullAsPathArgument() {
        String logManagerClass = FakeLogManager.class.getName();

        assertThatThrownBy(() -> new DeleteOnExitShutdownHook(null, logManagerClass, SHUTDOWN_METHOD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsLogManagerClassNameArgument() {
        Path path = directoryToDelete.toPath();

        assertThatThrownBy(() -> new DeleteOnExitShutdownHook(path, null, SHUTDOWN_METHOD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsShutdownMethodNameArgument() {
        String logManagerClass = FakeLogManager.class.getName();
        Path path = directoryToDelete.toPath();

        assertThatThrownBy(() -> new DeleteOnExitShutdownHook(path, logManagerClass, null))
            .isInstanceOf(NullPointerException.class);
    }
}
