/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class Log4j2LoggingFrameworkControl implements LoggingFrameworkControl {

    @NonNull
    private final Log4j2ShutdownPreprocessor log4j2ShutdownPreprocessor;
    @NonNull
    private final Log4j2Configurator log4j2Configurator;

    Log4j2LoggingFrameworkControl() {
        this(new Log4j2ShutdownPreprocessor(), new Log4j2Configurator());
    }

    @Override
    public void configure(ClassLoader applicationClassLoader, String applicationName) {
        log4j2Configurator.run(applicationClassLoader, applicationName);
    }

    @Override
    public void halt() {
        log4j2ShutdownPreprocessor.run();
    }

    @Override
    public boolean isBlockingWorkingDirectory() {
        return true;
    }
}
