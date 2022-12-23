package com.codeaffine.tiny.star.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.SystemPrintStreamCaptor;
import com.codeaffine.tiny.star.spi.CliCommandContract;

class StateCommandTest implements CliCommandContract<StateCommand> {

    @Override
    public StateCommand create() {
        return new StateCommand();
    }

    @Test
    @ExtendWith(SystemPrintStreamCaptor.SystemOutCaptor.class)
    void execute(SystemPrintStreamCaptor.SystemOutCaptor systemOutCaptor) {
        ApplicationServer applicationServer = CliCommandContract.stubApplicationServer();
        StateCommand actual = create();

        actual.execute(applicationServer);

        assertThat(systemOutCaptor.getLog())
            .contains(applicationServer.getState().name())
            .contains(applicationServer.getIdentifier());
    }

    @Override
    @ExtendWith(SystemPrintStreamCaptor.SystemOutCaptor.class)
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
