/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.Objects.nonNull;

import org.slf4j.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class StartInfoPrinter {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final Logger logger;

    StartInfoPrinter(ApplicationServer applicationServer) {
        this(applicationServer, getLogger(StartInfoPrinter.class));
    }

    void printStartText() {
        if (nonNull(applicationServer.startInfoProvider)) {
            applicationServer.startInfoProvider
                .apply(applicationServer)
                .lines()
                .forEach(logger::info);
        }
    }
}
