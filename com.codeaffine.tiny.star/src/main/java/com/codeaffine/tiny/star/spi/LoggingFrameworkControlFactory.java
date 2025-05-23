/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

/**
 * Factory interface for creating instances of {@link LoggingFrameworkControl}.
 *
 * @see LoggingFrameworkControl
 */
public interface LoggingFrameworkControlFactory {

    /**
     * Creates a new instance of {@link LoggingFrameworkControl}.
     *
     * @return a new instance of {@link LoggingFrameworkControl}
     */
    LoggingFrameworkControl create();
}
