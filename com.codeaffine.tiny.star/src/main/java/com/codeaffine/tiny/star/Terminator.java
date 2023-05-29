/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.shared.IoUtils.deleteDirectory;
import static com.codeaffine.tiny.shared.Threads.saveRun;
import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
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
