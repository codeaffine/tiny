/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import com.codeaffine.tiny.star.ApplicationServer;

import static com.codeaffine.tiny.demo.ApplicationServerContextRegistry.getApplicationServerContextRegistry;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

class ShutdownHandler {

    private final Object lock;

    private ApplicationServerContextRegistry registry;

    ShutdownHandler() {
        this.lock = new Object();
    }

    @ApplicationServer.Stopping
    void stopAllServers() {
        synchronized (lock) {
            if (nonNull(registry)) {
                ApplicationServerContextRegistry.stopAllServers(registry);
            }
        }
    }

    public void registerShutdownHook() {
        synchronized (lock) {
            if (isNull(registry)) {
                registry = getApplicationServerContextRegistry();
                Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> ApplicationServerContextRegistry.stopAllServers(registry)));
            }
        }
    }
}
