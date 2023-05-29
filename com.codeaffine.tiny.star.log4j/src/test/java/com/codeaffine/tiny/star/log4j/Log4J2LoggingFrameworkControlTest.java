/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class Log4J2LoggingFrameworkControlTest {

    private static final String APPLICATION_NAME = "applicationName";
    private Log4j2Configurator configurator;
    private Log4j2ShutdownPreprocessor shutdownPreprocessor;
    private Log4j2LoggingFrameworkControl loggingFrameworkControl;

    @BeforeEach
    void setUp() {
        shutdownPreprocessor = mock(Log4j2ShutdownPreprocessor.class);
        configurator = mock(Log4j2Configurator.class);
        loggingFrameworkControl = new Log4j2LoggingFrameworkControl(shutdownPreprocessor, configurator);
    }

    @Test
    void configure() {
        ClassLoader applicationClassLoader = mock(ClassLoader.class);

        loggingFrameworkControl.configure(applicationClassLoader, APPLICATION_NAME);

        verify(configurator).run(applicationClassLoader, APPLICATION_NAME);
    }

    @Test
    void halt() {
        loggingFrameworkControl.halt();

        verify(shutdownPreprocessor).run();
    }

    @Test
    void isUsingWorkingDirectory() {
        boolean actual = loggingFrameworkControl.isUsingWorkingDirectory();

        assertThat(actual).isTrue();
    }
}
