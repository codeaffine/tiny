/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpServlet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.engine.RWTServlet;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ApplicationServerConfiguration implements ServerConfiguration {

    @NonNull
    private final File workingDirectory;
    @NonNull
    private final ApplicationServer applicationServer;

    private ServletContextListenerAdapter servletContextListenerAdapter;

    @Override
    public SecureSocketLayerConfiguration getSecureSocketLayerConfiguration() {
        return applicationServer.secureSocketLayerConfiguration;
    }

    @Override
    public String getHost() {
        return applicationServer.host;
    }

    @Override
    public int getPort() {
        return applicationServer.port;
    }

    @Override
    public ClassLoader getContextClassLoader() {
        return applicationServer
            .applicationConfiguration
            .getClass()
            .getClassLoader();
    }

    @Override
    public ServletContextListener getContextListener() {
        if (isNull(servletContextListenerAdapter)) {
            servletContextListenerAdapter = new ServletContextListenerAdapter(
                new TinyStarServletContextListener(applicationServer.applicationConfiguration),
                applicationServer.servletContextListeners
            );
        }
        return servletContextListenerAdapter;
    }

    @Override
    public @NonNull File getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public Set<String> getEntryPointPaths() {
        return captureEntrypointPaths(applicationServer.applicationConfiguration);
    }

    @Override
    public List<FilterDefinition> getFilterDefinitions() {
        return applicationServer.filterDefinitions;
    }

    @Override
    public int getSessionTimeout() {
        return applicationServer.sessionTimeout;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends HttpServlet> Class<T> getHttpServletClass() {
        if (Objects.nonNull(applicationServer.rwtServletExtension)) {
            return (Class<T>) applicationServer.rwtServletExtension;
        }
        return (Class<T>) RWTServlet.class;
    }
}
