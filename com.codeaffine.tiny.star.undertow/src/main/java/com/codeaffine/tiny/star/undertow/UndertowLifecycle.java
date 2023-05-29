/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;

class UndertowLifecycle {

    private final AtomicReference<Undertow> serverHolder;
    private final String host;
    private final int port;

    UndertowLifecycle(@NonNull String host, int port) {
        this.serverHolder = new AtomicReference<>();
        this.host = host;
        this.port = port;
    }

    void startUndertow(@NonNull PathHandler path) {
        serverHolder.getAndUpdate(current -> doStart(path, current));
    }

    void stopUndertow() {
        serverHolder.getAndUpdate(UndertowLifecycle::doStop);
    }

    private Undertow doStart(PathHandler path, Undertow current) {
        if(nonNull(current)) {
            return current;
        }
        return doStart(path);
    }

    private Undertow doStart(PathHandler path) {
        Undertow result = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(path)
            .build();
        result.start();
        return result;
    }

    @SuppressWarnings("SameReturnValue")
    private static Undertow doStop(Undertow current) {
        if(nonNull(current)) {
            current.stop();
        }
        return null;
    }
}
