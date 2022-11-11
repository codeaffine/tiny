package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Files.deleteDirectory;
import static lombok.AccessLevel.PACKAGE;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A {@link Runtime#addShutdownHook(Thread)} handler that deletes the temporary
 * application resource directory on application shutdown
 * since {@link File#deleteOnExit()} did not work in all cases. In particular,
 * with Log4j2 some appenders do not shut down immediately and thus prevent the
 * deletion of the temporary directory.
 */
@RequiredArgsConstructor(access = PACKAGE)
class DeleteWorkingDirectoryOnExitHandler implements Runnable {

    @NonNull
    private final File directoryToDelete;
    @NonNull
    private final String logManagerClass;
    @NonNull
    private final String shutdownMethod;

    DeleteWorkingDirectoryOnExitHandler(File directoryToDelete) {
        this(directoryToDelete, "org.apache.logging.log4j.LogManager", "shutdown");
    }

    @Override
    public void run() {
        attemptToShutdownLog4jIfUsed();
        deleteDirectory(directoryToDelete);
    }

    private void attemptToShutdownLog4jIfUsed() {
        try {
            ClassLoader classLoader = Files.class.getClassLoader();
            Class<?> clazz = classLoader.loadClass(logManagerClass);
            Method method = clazz.getMethod(shutdownMethod);
            method.invoke(null);
        } catch (InvocationTargetException cause) {
            System.err.printf("Warning: Could not stop log4j. Problem during execution of %s.%s().", shutdownMethod, logManagerClass); // NOSONAR
            if (cause.getCause() instanceof RuntimeException runtimeException) {
                runtimeException.printStackTrace();
            } else {
                cause.printStackTrace();
            }
        } catch (ClassNotFoundException ignore) {
            // it seems log4j is not used, so hopefully we can safely ignore this...
        } catch (NoSuchMethodException | IllegalAccessException cause) {
            System.err.printf("Warning: Could not stop log4j. Unable to find or access method %s in class %s.", shutdownMethod, logManagerClass); // NOSONAR
        }
    }
}
