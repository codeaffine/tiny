/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpServlet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.util.Set;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class RwtServletRegistrar {

    static final String SERVLET_NAME = "rwtServlet";
    static final String ALL_SUB_PATHS_PATTERN = "/*";
    static final int LOAD_ON_STARTUP = 1;

    @NonNull
    private final Tomcat tomcat;
    @NonNull
    private final ServerConfiguration configuration;
    @NonNull
    private final ServletContextListener listener;

    RwtServletRegistrar(Tomcat tomcat, ServerConfiguration configuration) {
        this(tomcat, configuration, configuration.getContextListener());
    }

    void addRwtServlet(Context context) {
        HttpServlet servlet = newServlet();
        Wrapper wrapper = tomcat.addServlet(context.getPath(), SERVLET_NAME, servlet);
        wrapper.setLoadOnStartup(LOAD_ON_STARTUP);
        Set<String> entrypointPaths = configuration.getEntryPointPaths();
        entrypointPaths.forEach(path -> context.addServletMappingDecoded(path + ALL_SUB_PATHS_PATTERN, SERVLET_NAME));
        context.addServletContainerInitializer((classes, servletContext) -> listener.contextInitialized(new ServletContextEvent(servletContext)), null);
    }

    @SneakyThrows
    private HttpServlet newServlet() {
        return configuration
            .getHttpServletClass()
            .getConstructor()
            .newInstance();
    }
}
