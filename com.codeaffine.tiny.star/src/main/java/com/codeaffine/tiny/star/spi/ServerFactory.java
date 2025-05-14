/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

/**
 * Factory declaring the contract for creating a server instance.
 */
public interface ServerFactory {

    /**
     * Creates a new instance of {@code Server}.
     *
     * @param configuration the server configuration. Must not be null.
     * @return a new instance of {@code Server}. Never null.
     * @see Server
     * @see ServerConfiguration
     */
    Server create(ServerConfiguration configuration);
}
