package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.cli.Texts.*;
import static com.codeaffine.tiny.star.cli.Texts.ERROR_AWAITING_SHUT_DOWN_CLI;
import static lombok.AccessLevel.PACKAGE;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class ExecutorServiceAdapter {

    static final int TIMEOUT_AWAITING_TERMINATION = 30_000;

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
        executorService.shutdownNow();
        try {
            boolean terminated = executorService.awaitTermination(timeoutAwaitingTermination, MILLISECONDS);
            if (!terminated) {
                throw new IllegalStateException(ERROR_AWAITING_SHUT_DOWN_CLI);
            }
        } catch (InterruptedException cause) {
            currentThread().interrupt();
            throw new IllegalStateException(ERROR_SHUTTING_DOWN_CLI, cause);
        }
    }
}
