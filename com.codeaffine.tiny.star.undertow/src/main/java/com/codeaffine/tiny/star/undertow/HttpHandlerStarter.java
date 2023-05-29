/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
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
