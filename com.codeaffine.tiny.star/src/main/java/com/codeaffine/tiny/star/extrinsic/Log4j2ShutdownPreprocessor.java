package com.codeaffine.tiny.star.extrinsic;

import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.ApplicationServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class Log4j2ShutdownPreprocessor implements Runnable {

    @NonNull
    private final String logManagerClass;
    @NonNull
    private final String shutdownMethod;

    Log4j2ShutdownPreprocessor() {
        this("org.apache.logging.log4j.LogManager", "shutdown");
    }

    @Override
    public void run() {
        try {
            ClassLoader classLoader = ApplicationServer.class.getClassLoader();
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
