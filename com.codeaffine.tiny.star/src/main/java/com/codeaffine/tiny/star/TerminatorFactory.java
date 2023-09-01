/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.Server;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class TerminatorFactory {

    @NonNull
    private final ApplicationServer applicationServer;

    Terminator create(File workingDirectory, Server server, LoggingFrameworkControl loggingFrameworkControl, Runnable shutdownHookRemover) {
        return new Terminator(workingDirectory, server, loggingFrameworkControl, shutdownHookRemover, applicationServer);
    }
}
