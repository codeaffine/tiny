/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

import static java.lang.String.format;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ContextRegistrar {

    static final String CONTEXT_PATH = "";
    static final String DOC_BASE = "doc-base";

    @NonNull
    private final ServerConfiguration configuration;
    @NonNull
    private final Tomcat tomcat;

    Context addContext() {
        File docBase = new File(configuration.getWorkingDirectory(), DOC_BASE);
        if (!docBase.exists() && !docBase.mkdir()) {
            throw new IllegalStateException(format(Texts.ERROR_CREATE_DOC_BASE, docBase.getAbsolutePath()));
        }
        tomcat.setBaseDir(configuration.getWorkingDirectory().getAbsolutePath());
        Context result = tomcat.addContext(CONTEXT_PATH, docBase.getAbsolutePath());
        result.setSessionTimeout(configuration.getSessionTimeout());
        return result;
    }
}
