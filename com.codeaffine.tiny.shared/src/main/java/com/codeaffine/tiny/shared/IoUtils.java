/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import static com.codeaffine.tiny.shared.Texts.*;
import static com.codeaffine.tiny.shared.Threads.*;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class IoUtils {

    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }

    public static File createTemporayDirectory(@NonNull String directoryNamePrefix) throws IllegalArgumentException {
        if(directoryNamePrefix.isEmpty()) {
            throw new IllegalArgumentException(ERROR_DIRECTORY_NAME_PREFIX_IS_EMPTY);
        }
        try {
            return createTempDirectory(directoryNamePrefix + "-")
                .toFile();
        } catch (Exception cause) {
            throw new IllegalArgumentException(format(ERROR_UNABLE_TO_CREATE_TEMPORARY_DIRECTORY, directoryNamePrefix), cause);
        }
    }

    public static void deleteDirectory(@NonNull File toDelete) {
        try {
            doDelete(toDelete);
        } catch (IOException cause) {
            throw new IllegalArgumentException(format(ERROR_UNABLE_TO_DELETE_FILE, toDelete), cause);
        }
    }

    private static void doDelete(File toDelete) throws IOException {
        if(toDelete.exists()) {
            if (toDelete.isDirectory()) {
                doDeleteDirectory(toDelete);
            }
            deleteWithPotentialRacingCondition(toDelete);
        }
    }

    /* This method is a workaround for file deletion racing conditions which might occur
    when an RWT application cleans up its dynamically registered resources on application shutdown. */
    private static void deleteWithPotentialRacingCondition(File toDelete) throws IOException {
        try {
            delete(toDelete.toPath());
        } catch (Exception e) {
            sleepFor(10L);
            if(toDelete.exists()) { // check if the exception was caused by a racing condition on file deletion. If not, throw the exception.
                throw e;
            }
        }
    }

    private static void doDeleteDirectory(File toDelete) throws IOException {
        try (DirectoryStream<Path> children = newDirectoryStream(toDelete.toPath())) {
            for (Path child : children) {
                deleteDirectory(child.toFile());
            }
        }
    }
}
