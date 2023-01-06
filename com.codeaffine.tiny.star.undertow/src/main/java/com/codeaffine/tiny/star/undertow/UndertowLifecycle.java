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

    private static Undertow doStop(Undertow current) {
        if(nonNull(current)) {
            current.stop();
        }
        return null;
    }
}
