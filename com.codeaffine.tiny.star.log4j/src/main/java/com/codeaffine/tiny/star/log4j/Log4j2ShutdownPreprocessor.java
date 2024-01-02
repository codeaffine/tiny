/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.star.ApplicationServer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static lombok.AccessLevel.PACKAGE;

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
