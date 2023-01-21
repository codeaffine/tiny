package com.codeaffine.tiny.star.undertow;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class HttpHandlerStarter {

    static final String PREFIX_PATH = "/";

    @NonNull
    private final Supplier<PathHandler> pathHandlerFactory;

    HttpHandlerStarter() {
        this(Handlers::path);
    }

    PathHandler startRwtApplicationHttpHandler(@NonNull DeploymentManager manager) {
        HttpHandler httpHandler;
        try {
            httpHandler = manager.start();
        } catch (ServletException cause) {
            throw extractExceptionToReport(cause, IllegalStateException::new);
        }
        return pathHandlerFactory.get()
            .addPrefixPath(PREFIX_PATH, httpHandler);
    }
}
