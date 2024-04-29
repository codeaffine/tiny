package com.codeaffine.tiny.star.cli.spi.tck; /**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

public interface CliCommandContract<T extends CliCommand> {

    String APPLICATION_IDENTIFIER = "test-contract-application-identifier";
    String CLI_INSTANCE_SUFFIX = "cliInstanceSuffix";

    T create();

    @Test
    default void getCode() {
        T actual = create();

        assertThat(actual.getCode()).isNotEmpty();  // NOSONAR
    }

    @Test
    default void getName() {
        T actual = create();

        assertThat(actual.getName()).isNotEmpty();
    }

    @Test
    default void getDescription() {
        ApplicationServer serverInstance = stubApplicationServer();
        T command = create();

        T spy = spy(command);
        when(spy.getCode()).thenReturn(command.getCode() + CLI_INSTANCE_SUFFIX);
        String actual = command.getDescription(spy, serverInstance);

        assertDescription(assertThat(actual), spy, serverInstance);
    }

    default void assertDescription(AbstractStringAssert<?> description, CliCommand command, ApplicationServer applicationServer) {
        description.contains(command.getCode());
        description.contains(applicationServer.getIdentifier());
    }

    @Test
    default void execute() {
        T actual = create();

        assertThatNoException().isThrownBy(() -> actual.execute(stubApplicationServer(), emptyMap()));
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
