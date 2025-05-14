/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

/**
 * This interface specifies the contract for a server. A Server encapsulates an embedded
 * servlet container and controls the lifecycle of this container. This
 * means that a server instance can be started and stopped.
 *
 * <p>Implementations of this interface must be thread-safe.</p>
 */
public interface Server {

    /**
     * Starts the server. This method must be called before the server can
     * accept incoming requests.
     */
  void start();
    /**
     * Stops the server. This method must be called to release any resources
     * held by the server and to stop accepting incoming requests.
     */
  void stop();

    /**
     * Returns the server's name.
     *
     * @return the name of the server. Never <code>null</code>.
     */
  String getName();
}
