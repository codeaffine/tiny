/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

@RequiredArgsConstructor(access = PACKAGE)
class StartInfoPrinter {

    static Logger logger = getLogger(StartInfoPrinter.class);

    @NonNull
    private final ApplicationServer applicationServer;

    void printStartText() {
        if (nonNull(applicationServer.startInfoProvider)) {
            applicationServer.startInfoProvider
                .apply(applicationServer)
                .lines()
                .forEach(logger::info);
        }
    }
}
