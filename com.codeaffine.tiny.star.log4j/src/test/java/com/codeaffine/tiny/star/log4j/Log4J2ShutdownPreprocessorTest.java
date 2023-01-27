package com.codeaffine.tiny.star.log4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.test.SystemPrintStreamCaptor.SystemErrCaptor;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class Log4J2ShutdownPreprocessorTest {

    private static final String UNKNOWN_LOG_MANAGER_CLASS = "unknownLogManagerClass";
    private static final String LOG_MANAGER_CLASS = FakeLogManager.class.getName();
    private static final String SHUTDOWN_METHOD = "shutdown";
    private static final String WRONG_METHOD = "wrongMethod";
    private static final String ERROR_MESSAGE = "bad";

    static class FakeLogManager {

        static final AtomicReference<Exception> problemHolder = new AtomicReference<>();
        static final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

        static void initializeFakeLogManager() {
            shutdownCalled.set(false);
            problemHolder.set(null);
        }

        @SuppressWarnings("unused")
        public static void shutdown() throws Exception {
            if(nonNull(problemHolder.get())) {
                throw problemHolder.get();
            }
            shutdownCalled.set(true);
        }
    }

    @BeforeEach
    void setUp() {
        FakeLogManager.initializeFakeLogManager();
    }

    @Test
    void run() {
        Log4j2ShutdownPreprocessor preprocessor = new Log4j2ShutdownPreprocessor(LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        preprocessor.run();

        assertThat(FakeLogManager.shutdownCalled).isTrue();
    }

    @Test
    void runIfLog4JIsNotUsed() {
        Log4j2ShutdownPreprocessor preprocessor = new Log4j2ShutdownPreprocessor(UNKNOWN_LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        preprocessor.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void runIfShutdownMethodIsNotFoundOrUnAccessible(SystemErrCaptor systemErrCaptor) {
        Log4j2ShutdownPreprocessor preprocessor = new Log4j2ShutdownPreprocessor(LOG_MANAGER_CLASS, WRONG_METHOD);

        preprocessor.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(systemErrCaptor.getLog())
            .contains(LOG_MANAGER_CLASS)
            .contains(WRONG_METHOD);
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void runIfShutdownMethodThrowsRuntimeException(SystemErrCaptor systemErrCaptor) {
        FakeLogManager.problemHolder.set(new RuntimeException(ERROR_MESSAGE));
        Log4j2ShutdownPreprocessor preprocessor = new Log4j2ShutdownPreprocessor(LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        preprocessor.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(systemErrCaptor.getLog())
            .contains(LOG_MANAGER_CLASS)
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void runIfShutdownMethodThrowsException(SystemErrCaptor systemErrCaptor) {
        FakeLogManager.problemHolder.set(new Exception(ERROR_MESSAGE));
        Log4j2ShutdownPreprocessor preprocessor = new Log4j2ShutdownPreprocessor(LOG_MANAGER_CLASS, SHUTDOWN_METHOD);

        preprocessor.run();

        assertThat(FakeLogManager.shutdownCalled).isFalse();
        assertThat(systemErrCaptor.getLog())
            .contains(LOG_MANAGER_CLASS)
            .contains(SHUTDOWN_METHOD)
            .contains(ERROR_MESSAGE);
    }
}
