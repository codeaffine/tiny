/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.codeaffine.tiny.star.undertow.Texts.SERVER_NAME;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ServerImpl implements Server {

    @NonNull
    private final ServletInfoFactory servletInfoFactory;
    @NonNull
    private final DeploymentOperation deploymentOperation;
    @NonNull
    private final HttpHandlerStarter httpHandlerStarter;
    @NonNull
    private final UndertowLifecycle undertowLifecycle;

    ServerImpl(ServerConfiguration configuration) {
        this(
            new ServletInfoFactory(configuration),
            new DeploymentOperation(configuration),
            new HttpHandlerStarter(),
            new UndertowLifecycle(configuration)
        );
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void start() {
        ServletInfo servletInfo = servletInfoFactory.createRwtServletInfo();
        DeploymentManager manager = deploymentOperation.deployRwtApplication(servletInfo);
        PathHandler path = httpHandlerStarter.startRwtApplicationHttpHandler(manager);
        undertowLifecycle.startUndertow(path);
    }

    @Override
    public void stop() {
        undertowLifecycle.stopUndertow();
    }
}
