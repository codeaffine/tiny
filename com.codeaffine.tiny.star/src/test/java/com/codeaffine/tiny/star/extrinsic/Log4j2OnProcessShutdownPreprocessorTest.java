package com.codeaffine.tiny.star.extrinsic;

import static com.codeaffine.tiny.star.extrinsic.Log4j2OnProcessShutdownPreprocessorTest.FakeLogManager.initializeFakeLogManager;
import static org.assertj.core.api.Assertions.assertThat;

import static java.util.Objects.nonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class Log4j2OnProcessShutdownPreprocessorTest {

    private static final String UNKNOWN_LOG_MANAGER_CLASS = "unknownLogManagerClass";
    private static final String LOG_MANAGER_CLASS = FakeLogManager.class.getName();
    private static final String SHUTDOWN_METHOD = "shutdown";
    private static final String WRONG_METHOD = "wrongMethod";
    private static final String ERROR_MESSAGE = "bad";

    private PrintStream errorPrintStreamBuffer;
    private ByteArrayOutputStream errorOut;

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
    void setUp() {
        errorPrintStreamBuffer = System.err;
        errorOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOut));
        initializeFakeLogManager();
    }

    @AfterEach
    void tearDown() {
        System.setErr(errorPrintStreamBuffer);
    }

    @Test
    void run() {
        Log4j2OnProcessShutdownPreprocessor handler = new Log4j2OnProcessShutdownPreprocessor(LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isTrue();
    }

    @Test
    void runIfLog4JIsNotUsed() {
        Log4j2OnProcessShutdownPreprocessor handler = new Log4j2OnProcessShutdownPreprocessor(UNKNOWN_LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
    }

    @Test
    void runIfShutdownMethodIsNotFoundOrUnaccessible() {
        Log4j2OnProcessShutdownPreprocessor handler = new Log4j2OnProcessShutdownPreprocessor(LOG_MANAGER_CLASS, WRONG_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(errorOut.toString())
            .contains(LOG_MANAGER_CLASS)
            .contains(WRONG_METHOD);
    }

    @Test
    void runIfShutdownMethodThrowsRuntimeException() {
        FakeLogManager.problemHolder.set(new RuntimeException(ERROR_MESSAGE));
        Log4j2OnProcessShutdownPreprocessor handler = new Log4j2OnProcessShutdownPreprocessor(LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(errorOut.toString())
            .contains(LOG_MANAGER_CLASS)
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    void runIfShutdownMethodThrowsException() {
        FakeLogManager.problemHolder.set(new Exception(ERROR_MESSAGE));
        Log4j2OnProcessShutdownPreprocessor handler = new Log4j2OnProcessShutdownPreprocessor(LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        handler.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(errorOut.toString())
            .contains(LOG_MANAGER_CLASS)
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }
}
