/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.spec.ServletContextImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static java.util.Objects.nonNull;

class UndertowLifecycle {

    private final ProtocolListenerApplicator protocolListenerApplicator;
    private final AtomicReference<UndertowInstance> serverHolder;
    private final ServerConfiguration configuration;

    record UndertowInstance(Undertow undertow, DeploymentManager manager) {}

    UndertowLifecycle(@NonNull ServerConfiguration configuration) {
        this.configuration = configuration;
        this.serverHolder = new AtomicReference<>();
        this.protocolListenerApplicator = new ProtocolListenerApplicator(configuration);
    }

    void startUndertow(@NonNull PathHandler path, DeploymentManager manager) {
        serverHolder.getAndUpdate(current -> doStart(path, manager, current));
    }

    void stopUndertow() {
        serverHolder.getAndUpdate(this::doStop);
    }

    private UndertowInstance doStart(PathHandler path, DeploymentManager manager, UndertowInstance current) {
        if(nonNull(current)) {
            return current;
        }
        return doStart(path, manager);
    }

    private UndertowInstance doStart(PathHandler path, DeploymentManager manager) {
        Undertow undertow = protocolListenerApplicator.addListener(Undertow.builder())
            .setHandler(path)
            .build();
        undertow.start();
        return new UndertowInstance(undertow, manager);
    }

    @SuppressWarnings("SameReturnValue")
    private UndertowInstance doStop(UndertowInstance current) {
        if(nonNull(current)) {
            stopManager(current);
            current.undertow().stop();
        }
        return null;
    }

    private void stopManager(UndertowInstance current) {
        try {
            DeploymentManager manager = current.manager();
            destroyContext(manager);
            manager.stop();
        } catch (ServletException servletException) {
            throw extractExceptionToReport(servletException, IllegalStateException::new);
        }
    }

    private void destroyContext(DeploymentManager manager) {
        // is it really necessary to destroy the context manually? Could not find a better way to do it.
        Deployment deployment = manager.getDeployment();
        ServletContextImpl servletContext = deployment.getServletContext();
        configuration.getContextListener().contextDestroyed(new ServletContextEvent(servletContext));
    }
}
