package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PRIVATE;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.Files.createTempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Files {

    public static File createTemporayDirectory(String name) {
        try {
            String directorNamePrefix = name + "-";
            Path path = createTempDirectory(directorNamePrefix);
            File result = path.toFile();
            Runnable shutdown = new DeleteWorkingDirectoryOnExitHandler(result);
            getRuntime().addShutdownHook(new Thread(shutdown));
            return result;
        } catch (IOException cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    public static void deleteDirectory(File toDelete) {
        if(toDelete.exists()) {
            if (toDelete.isDirectory()) {
                try (DirectoryStream<Path> children = newDirectoryStream(toDelete.toPath())) {
                    for (Path child : children) {
                        deleteDirectory(child.toFile());
                    }
                } catch (IOException cause) {
                    throw new IllegalArgumentException(format("Could not open directory stream for '%s'.", toDelete), cause);
                }
            }
            try {
                delete(toDelete.toPath());
            } catch (IOException cause) {
                throw new IllegalArgumentException(format("Could not delete file or directory '%s'.", toDelete), cause);
            }
        }
    }
}
