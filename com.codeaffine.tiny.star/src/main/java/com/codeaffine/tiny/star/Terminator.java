package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.shared.IoUtils.deleteDirectory;
import static com.codeaffine.tiny.shared.Threads.saveRun;
import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(access = PACKAGE)
class Terminator implements Runnable {

    @NonNull
    private final File applicationWorkingDirectory;
    @NonNull
    private final Server server;
    @NonNull
    private final LoggingFrameworkControl loggingFrameworkControl;
    @NonNull
    private final Runnable shutdownHookRemover;
    private final boolean deleteWorkingDirectoryOnShutdown;

    @Getter
    @Setter
    private boolean shutdownHookExecution;

    @Override
    public void run() {
        server.stop();
        System.getProperties().remove(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY);
        deleteWorkingDirectory();
        shutdownHookRemover.run();
    }

    void deleteWorkingDirectory() {
        if (deleteWorkingDirectoryOnShutdown) {
            if (isShutdownHookExecution()) {
                saveRun(loggingFrameworkControl::halt);
                deleteDirectory(applicationWorkingDirectory);
            } else {
                if(!loggingFrameworkControl.isUsingWorkingDirectory()) {
                    deleteDirectory(applicationWorkingDirectory);
                }
            }
        }
    }
}
