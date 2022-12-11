package com.codeaffine.tiny.star.spi;

import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;

import com.codeaffine.tiny.star.ApplicationServer;

public interface CliCommandContract<T extends CliCommand> {

    String APPLICATION_IDENTIFIER = "test-contract-application-identifier";

    T create();

    @Test
    default void getCode() {
        T actual = create();

        assertThat(actual.getCode()).isNotEmpty();
    }

    @Test
    default void getName() {
        T actual = create();

        assertThat(actual.getName()).isNotEmpty();
    }

    @Test
    default void getDescription() {
        ApplicationServer serverInstance = stubApplicationServer();
        T actual = create();

        AbstractStringAssert<?> descriptionAssert = assertThat(actual.getDescription(actual, serverInstance));
        descriptionAssert.isNotEmpty();
        assertDescription(descriptionAssert, actual, serverInstance);
    }

    default void assertDescription(AbstractStringAssert<?> description, CliCommand command, ApplicationServer applicationServer) {
        // subclasses may override
    }

    @Test
    default void execute() {
        T actual = create();

        assertThatNoException().isThrownBy(() -> actual.execute(stubApplicationServer()));
    }

    @Test
    default void getId() {
        T actual = create();

        assertThat(actual.getId()).isNotEmpty();
    }

    @Test
    default void isHelpCommand() {
        T actual = create();

        assertThat(actual.isHelpCommand()).isEqualTo(isExpectedToBeHelpCommand());
    }

    default boolean isExpectedToBeHelpCommand() {
        return false;
    }

    @Test
    default void printHelpOnStartup() {
        T actual = create();

        assertThat(actual.printHelpOnStartup()).isEqualTo(isExpectedToPrintHelpOnStartup());
    }

    default boolean isExpectedToPrintHelpOnStartup() {
        return false;
    }

    static ApplicationServer stubApplicationServer() {
        ApplicationServer result = mock(ApplicationServer.class);
        when(result.getState()).thenReturn(RUNNING);
        when(result.getIdentifier()).thenReturn(APPLICATION_IDENTIFIER);
        return result;
    }
}
