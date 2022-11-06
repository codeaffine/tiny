package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.logging.LogManager;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Files {

    @RequiredArgsConstructor(access = PACKAGE)
    static class DeleteOnExitShutdownHook implements Runnable {

        @NonNull
        private final Path path;

        @Override
        public void run() {
            stopLog4jIfAvailable();
            deleteDirectory(path);
        }

        private static void stopLog4jIfAvailable() {
            try {
                ClassLoader classLoader = Files.class.getClassLoader();
                Class<?> clazz = classLoader.loadClass("org.apache.logging.log4j.LogManager");
                if (nonNull(clazz)) {
                    clazz.getMethod("shutdown").invoke(null);
                }
            } catch (RuntimeException cause) {
                throw cause;
            } catch (InvocationTargetException cause) {
                if (cause.getCause() instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException(cause);
            } catch (Exception cause) {
                throw new IllegalStateException(cause);
            }
        }
    }

    public static File createTemporayDirectory(String name) {
        try {
            String directorNamePrefix = name + "-";
            Path path = createTempDirectory(directorNamePrefix);
            File result = path.toFile();
            Runnable shutdown = new DeleteOnExitShutdownHook(path);
            getRuntime().addShutdownHook(new Thread(shutdown));
            return result;
        } catch (IOException cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    public static void deleteDirectory(Path path) {
        if (isDirectory(path, NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            } catch (IOException cause) {
                cause.printStackTrace();
                throw new IllegalArgumentException(format("Could not delete directory %s.", path), cause);
            }
        }
        try {
            delete(path);
        } catch (IOException cause) {
            cause.printStackTrace();
            throw new IllegalArgumentException(format("Could not delete file or directory %s.", path), cause);
        }
    }
}
