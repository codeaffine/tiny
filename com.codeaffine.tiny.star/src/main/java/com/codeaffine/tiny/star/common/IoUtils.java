package com.codeaffine.tiny.star.common;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import static com.codeaffine.tiny.star.common.Texts.*;
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
            delete(toDelete.toPath());
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
