package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ShutdownHookRemover implements Runnable {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final LoggingFrameworkControl loggingFrameworkControl;
    @NonNull
    private final ShutdownHookHandler shutdownHookHandler;
    @NonNull
    private final AtomicReference<Runnable> shutdownHookOperation;

    @Override
    public void run() {
        if (!loggingFrameworkControl.isUsingWorkingDirectory() || !applicationServer.deleteWorkingDirectoryOnShutdown) {
            shutdownHookHandler.deregister(shutdownHookOperation.get());
        }
    }
}
