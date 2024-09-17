/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.test.test.fixtures.system.io.SystemPrintStreamCaptor.SystemErrCaptor;
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
