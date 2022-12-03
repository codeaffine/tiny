package com.codeaffine.tiny.star.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.SystemPrintStreamCaptor;
import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandContract;

class StateCommandTest implements CliCommandContract<StateCommand> {

    @Override
    public StateCommand create() {
        return new StateCommand();
    }

    @Test
    @ExtendWith(SystemPrintStreamCaptor.SystemOutCaptor.class)
    void execute(SystemPrintStreamCaptor.SystemOutCaptor systemOutCaptor) {
        ApplicationInstance applicationInstance = CliCommandContract.stubApplicationInstance();
        StateCommand actual = create();

        actual.execute(applicationInstance);

        assertThat(systemOutCaptor.getLog())
            .contains(applicationInstance.getState().name())
            .contains(applicationInstance.getIdentifier());
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
        ApplicationInstance applicationInstance = CliCommandContract.stubApplicationInstance();
        StateCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(null, applicationInstance))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsApplicationInstanceArgument() {
        StateCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(actual, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Override
    public void assertDescription(AbstractStringAssert<?> description, CliCommand command, ApplicationInstance applicationInstance) {
        description
            .contains(command.getCode())
            .contains(applicationInstance.getIdentifier());
    }
}
