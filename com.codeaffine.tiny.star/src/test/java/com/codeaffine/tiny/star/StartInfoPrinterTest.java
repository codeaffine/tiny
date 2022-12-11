package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.Logger;

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
        ApplicationServer applicationServer = newApplicationServerBuilder(configuration -> {})
            .withApplicationIdentifier(APPLICATION_IDENTIFIER)
            .build();
        StartInfoPrinter printer = new StartInfoPrinter(applicationServer, logger);

        printer.printStartText();

        verify(logger, atLeastOnce()).info(anyString());
    }

    @Test
    void printStartTextWithParticularStartTextProviderSet() {
        ApplicationServer applicationServer = newApplicationServerBuilder(configuration -> {})
            .withStartInfoProvider(server -> String.format(START_TEXT, server.getIdentifier()))
            .withApplicationIdentifier(APPLICATION_IDENTIFIER)
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
        ApplicationServer applicationServer = newApplicationServerBuilder(configuration -> {})
            .withStartInfoProvider(null)
            .withApplicationIdentifier(APPLICATION_IDENTIFIER)
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
