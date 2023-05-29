/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.http.HttpServlet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.util.Set;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class RwtServletRegistrar {

    static final String SERVLET_NAME = "rwtServlet";
    static final String ALL_SUB_PATHS_PATTERN = "/*";

    @NonNull
    private final Tomcat tomcat;
    @NonNull
    private final ApplicationConfiguration applicationConfiguration;
    @NonNull
    private final TinyStarServletContextListener listener;

    RwtServletRegistrar(Tomcat tomcat, ApplicationConfiguration applicationConfiguration) {
        this(tomcat, applicationConfiguration, new TinyStarServletContextListener(applicationConfiguration));
    }

    void addRwtServlet(Context context) {
        HttpServlet servlet = new RwtServletAdapter();
        tomcat.addServlet(context.getPath(), SERVLET_NAME, servlet);
        Set<String> entrypointPaths = captureEntrypointPaths(applicationConfiguration);
        entrypointPaths.forEach(path -> context.addServletMappingDecoded(path + ALL_SUB_PATHS_PATTERN, SERVLET_NAME));
        context.addServletContainerInitializer((classes, servletContext) -> listener.contextInitialized(new ServletContextEvent(servletContext)), null);
    }
}
