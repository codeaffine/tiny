package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Files.deleteDirectory;
import static lombok.AccessLevel.PACKAGE;

import java.io.File;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class is intended to be used as a {@link Runtime#addShutdownHook(Thread)}
 * handler to delete the application's working directory on process shutdown
 * if requested. {@link File#deleteOnExit()} did not do the trick. For example,
 * some Log4j2 appenders do not shut down immediately and thus might prevent the
 * deletion of the directory.
 */
@RequiredArgsConstructor(access = PACKAGE)
class DeleteWorkingDirectoryOnProcessShutdownHandler implements Runnable {

    @NonNull
    private final File directoryToDelete;
    @NonNull
    private final List<Runnable> deleteWorkingDirectoryOnProcessShutdownPreprocessors;

    @Override
    public void run() {
        deleteWorkingDirectoryOnProcessShutdownPreprocessors.forEach(this::saveRun);
        deleteDirectory(directoryToDelete);
    }

    private void saveRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception cause) {
            System.err.printf("Warning: Could not execute preprocessor %s.", runnable.getClass().getName()); // NOSONAR
            cause.printStackTrace();
        }
    }
}
