package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationRunner.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.IoUtils.deleteDirectory;
import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import java.util.List;
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
    private final List<Runnable> deleteWorkingDirectoryOnProcessShutdownPreprocessors;
    private final boolean deleteWorkingDirectoryOnShutdown;

    @Getter
    @Setter
    private boolean shutdownHookExecution;

    @Override
    public void run() {
        server.stop();
        System.getProperties().remove(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY);
        deleteWorkingDirectory();
    }

    void deleteWorkingDirectory() {
        if (deleteWorkingDirectoryOnShutdown) {
            if (isShutdownHookExecution()) {
                deleteWorkingDirectoryOnProcessShutdownPreprocessors.forEach(Terminator::saveRun);
                deleteDirectory(applicationWorkingDirectory);
            } else {
                if(deleteWorkingDirectoryOnProcessShutdownPreprocessors.isEmpty()) {
                    deleteDirectory(applicationWorkingDirectory);
                }
            }
        }
    }

    private static void saveRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception cause) {
            System.err.printf("Warning: Could not execute preprocessor %s.", runnable.getClass().getName()); // NOSONAR
            cause.printStackTrace();
        }
    }
}
