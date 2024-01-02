/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class LoggingFrameworkConfigurator {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final LoggingFrameworkControlFactory loggingFrameworkControlFactory;

    LoggingFrameworkConfigurator(ApplicationServer applicationServer) {
        this(applicationServer, new DelegatingLoggingFrameworkControlFactory());
    }

    LoggingFrameworkControl configureLoggingFramework() {
        ClassLoader applicationClassLoader = applicationServer.applicationConfiguration.getClass().getClassLoader();
        LoggingFrameworkControl result = loggingFrameworkControlFactory.create();
        result.configure(applicationClassLoader, applicationServer.getIdentifier());
        return result;
    }
}
