package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PACKAGE;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class Engine {

    @NonNull
    private final ExecutorServiceAdapter executor;
    @NonNull
    private final InputScanner inputScanner;

    void start() {
        executor.execute(inputScanner::scanForCommandCode);
    }

    void stop() {
        inputScanner.cancel();
        executor.stop();
    }
}
