/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class SessionTimeoutConfigurator implements ServletContainerInitializer {

    @NonNull
    private final ServerConfiguration configuration;

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        servletContext.setSessionTimeout(configuration.getSessionTimeout());
    }

    static ServletContainerInitializerInfo newServletContainerInitializerInfo(ServerConfiguration configuration) {
        return new ServletContainerInitializerInfo(
            SessionTimeoutConfigurator.class,
            new ImmediateInstanceFactory<>(new SessionTimeoutConfigurator(configuration)),
            null);
    }
}
