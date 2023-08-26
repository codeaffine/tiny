/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.Logger;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StartInfoPrinterTest {

    private static final String APPLICATION_IDENTIFIER = "com.codeaffine.tiny.star";
    private static final String START_TEXT
        = """
        Multi
        Line
        Text
        %s
        """;

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
    }

    @Test
    void printStartText() {
        ApplicationServer applicationServer = newApplicationServerBuilder(configuration -> {}, APPLICATION_IDENTIFIER)
            .build();
        StartInfoPrinter printer = new StartInfoPrinter(applicationServer, logger);

        printer.printStartText();

        verify(logger, atLeastOnce()).info(anyString());
    }

    @Test
    void printStartTextWithParticularStartTextProviderSet() {
        ApplicationServer applicationServer = newApplicationServerBuilder(configuration -> {}, APPLICATION_IDENTIFIER)
            .withStartInfoProvider(server -> String.format(START_TEXT, server.getIdentifier()))
            .build();
        StartInfoPrinter printer = new StartInfoPrinter(applicationServer, logger);

        printer.printStartText();

        InOrder order = inOrder(logger);
        order.verify(logger).info("Multi");
        order.verify(logger).info("Line");
        order.verify(logger).info("Text");
        order.verify(logger).info(APPLICATION_IDENTIFIER);
        order.verifyNoMoreInteractions();
    }

    @Test
    void printStartTextIfStartTextProviderIsSetToNull() {
        ApplicationServer applicationServer = newApplicationServerBuilder(configuration -> {}, APPLICATION_IDENTIFIER)
            .withStartInfoProvider(null)
            .build();
        StartInfoPrinter printer = new StartInfoPrinter(applicationServer, logger);

        printer.printStartText();

        verifyNoInteractions(logger);
    }

    @Test
    void constructWithNullAsApplicationServerArgument() {
        assertThatThrownBy(() -> new StartInfoPrinter(null))
            .isInstanceOf(NullPointerException.class);
    }
}
