/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.test.fixtures.CliCommandContract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeaffine.tiny.test.test.fixtures.system.io.SystemPrintStreamCaptor.SystemOutCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StateCommandTest implements CliCommandContract<StateCommand> {

    @Override
    public StateCommand create() {
        return new StateCommand();
    }

    @Test
    @ExtendWith(SystemOutCaptor.class)
    void execute(SystemOutCaptor systemOutCaptor) {
        ApplicationServer applicationServer = CliCommandContract.stubApplicationServer();
        StateCommand actual = create();

        actual.execute(applicationServer);

        assertThat(systemOutCaptor.getLog())
            .contains(applicationServer.getState().name())
            .contains(applicationServer.getIdentifier());
    }

    @Override
    @ExtendWith(SystemOutCaptor.class)
    @SuppressWarnings({"EmptyMethod", "unused"})
    public void execute() {
        CliCommandContract.super.execute();
    }

    @Test
    void executeWithNullAsApplicationInstanceArgument() {
        StateCommand actual = create();

        assertThatThrownBy(() -> actual.execute(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsCommandArgument() {
        ApplicationServer applicationServer = CliCommandContract.stubApplicationServer();
        StateCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(null, applicationServer))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsApplicationInstanceArgument() {
        StateCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(actual, null))
            .isInstanceOf(NullPointerException.class);
    }
}
