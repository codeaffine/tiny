/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
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
import static com.codeaffine.tiny.shared.Threads.sleepFor;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * Utility class for performing input/output related operations.
 */
@NoArgsConstructor(access = PRIVATE)
public class IoUtils {

    /**
     * Finds a free port on the local machine.
     *
     * @return A free port number, or -1 if unable to find a free port.
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Creates a temporary directory with the specified prefix.
     *
     * @param directoryNamePrefix The prefix for the temporary directory name. Must not be null or empty.
     * @return The created temporary directory as a {@link File} object.
     * @throws IllegalArgumentException If the provided prefix is empty or if an error occurs during directory creation.
     */
    public static File createTemporaryDirectory(@NonNull String directoryNamePrefix) throws IllegalArgumentException {
        if(directoryNamePrefix.isEmpty()) {
            throw new IllegalArgumentException(ERROR_DIRECTORY_NAME_PREFIX_IS_EMPTY);
        }
        try {
            return createTempDirectory(directoryNamePrefix + "-") // NOSONAR: creation of temporary directory is intended
                .toFile();
        } catch (Exception cause) {
            throw new IllegalArgumentException(format(ERROR_UNABLE_TO_CREATE_TEMPORARY_DIRECTORY, directoryNamePrefix), cause);
        }
    }

    /**
     * Deletes the specified directory and all its contents.
     *
     * @param toDelete The directory to delete. Must not be null.
     * @throws IllegalArgumentException If an error occurs during deletion.
     */
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
