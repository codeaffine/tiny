/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.servlet.api.ServletInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static io.undertow.servlet.Servlets.servlet;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ServletInfoFactory {

    static final String SERVLET_NAME = "rwtServlet";
    static final String ALL_SUB_PATHS_PATTERN = "/*";
    static final int LOAD_ON_STARTUP = 0;

    @NonNull
    private final ServerConfiguration configuration;

    ServletInfo createRwtServletInfo() {
        ServletInfo result = servlet(SERVLET_NAME, RwtServletAdapter.class);
        result.setLoadOnStartup(LOAD_ON_STARTUP);
        Set<String> entrypointPaths = configuration.getEntryPointPaths();
        entrypointPaths.forEach(path -> result.addMapping(path + ALL_SUB_PATHS_PATTERN));
        return result;
    }
}
