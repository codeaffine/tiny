/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

import static com.codeaffine.tiny.star.cli.Texts.*;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ExecutorServiceAdapter {

    static final int TIMEOUT_AWAITING_TERMINATION = 5000;

    @NonNull
    private final ExecutorService executorService;
    private final int timeoutAwaitingTermination;

    Thread shutdownHandler;

    ExecutorServiceAdapter(ExecutorService executorService) {
        this(executorService, TIMEOUT_AWAITING_TERMINATION);
    }

    void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    void stop() {
        shutdownHandler = new Thread(this::handleExecutorServiceShutdown, SHUTDOWN_THREAD_NAME);
        shutdownHandler.start();
    }

    private void handleExecutorServiceShutdown() {
        executorService.shutdown();
        try {
            boolean terminated = executorService.awaitTermination(timeoutAwaitingTermination / 2, MILLISECONDS);
            if (!terminated) {
                executorService.shutdownNow();
            }
            terminated = executorService.awaitTermination(timeoutAwaitingTermination / 2, MILLISECONDS);
            if (!terminated) {
                throw new IllegalStateException(ERROR_AWAITING_SHUT_DOWN_CLI);
            }
        } catch (InterruptedException cause) {
            currentThread().interrupt();
            throw new IllegalStateException(ERROR_SHUTTING_DOWN_CLI, cause);
        }
    }
}
