package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PRIVATE;

import static java.lang.String.format;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newDirectoryStream;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
class IoUtils {

    static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }

    static File createTemporayDirectory(@NonNull String directoryNamePrefix) throws IllegalArgumentException {
        if(directoryNamePrefix.isEmpty()) {
            throw new IllegalArgumentException("directoryNamePrefix must not be empty");
        }
        try {
            return createTempDirectory(directoryNamePrefix + "-")
                .toFile();
        } catch (Exception cause) {
            throw new IllegalArgumentException(format("unable to create temporay directory with filename prefix %s", directoryNamePrefix), cause);
        }
    }

    static void deleteDirectory(@NonNull File toDelete) {
        try {
            doDelete(toDelete);
        } catch (IOException cause) {
            throw new IllegalArgumentException(format("Could not delete file or directory '%s'.", toDelete), cause);
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
