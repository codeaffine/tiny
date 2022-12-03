package com.codeaffine.tiny.star.cli;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandContract;

class QuitCommandTest implements CliCommandContract<QuitCommand> {

    @Override
    public QuitCommand create() {
        return new QuitCommand();
    }

    @Override
    public boolean isExpectedToPrintHelpOnStartup() {
        return true;
    }

    @Override
    public void assertDescription(AbstractStringAssert<?> description, CliCommand command, ApplicationInstance applicationInstance) {
        description
            .contains(command.getCode())
            .contains(applicationInstance.getIdentifier());
    }
    
    @Test
    void executeWithNullAsApplicationInstanceArgument() {
        QuitCommand actual = create();
        
        assertThatThrownBy(() -> actual.execute(null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    void getDescriptionWithNullAsCommandArgument() {
        ApplicationInstance applicationInstance = CliCommandContract.stubApplicationInstance();
        QuitCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(null, applicationInstance))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsApplicationInstanceArgument() {
        QuitCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(actual, null))
            .isInstanceOf(NullPointerException.class);
    }
}
