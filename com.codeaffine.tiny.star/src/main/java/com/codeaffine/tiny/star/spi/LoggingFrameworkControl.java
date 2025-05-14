/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

/**
 * This interface specifies the contract for a logging framework control. A LoggingFrameworkControl instance
 * is responsible for configuring and controlling the lifecycle of the underlying logging framework.
 */
public interface LoggingFrameworkControl {

    /**
     * Configures the logging framework with the specified class loader and application name.
     *
     * @param applicationClassLoader the class loader to be used for loading classes of the RWT application. Never null.
     * @param applicationName        the name of the application. Never null.
     */
    default void configure(ClassLoader applicationClassLoader, String applicationName) {}

    /**
     * stops the logging framework.
     */
    default void halt() {}

    /**
     * some logging frameworks block working directory deletion on server shutdown and
     * therefore need special handling.
     *
     * @return true if the logging framework blocks working directory deletion on server shutdown, false otherwise. Default is false.
     */
    default boolean isBlockingWorkingDirectory() {
        return false;
    }
}
