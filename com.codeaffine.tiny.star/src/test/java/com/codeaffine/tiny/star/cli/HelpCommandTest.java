package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemOutCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandContract;

import java.util.Set;

class HelpCommandTest implements CliCommandContract<HelpCommand> {

    private DelegatingCliCommandProvider delegatingCliCommandProvider;

    @BeforeEach
    void setUp() {
        delegatingCliCommandProvider = mock(DelegatingCliCommandProvider.class);
    }

    @Test
    @ExtendWith(SystemOutCaptor.class)
    void execute(SystemOutCaptor systemOutCaptor) {
        ApplicationInstance applicationInstance = CliCommandContract.stubApplicationInstance();
        TestCliCommand command = new TestCliCommand();
        stubCliCommandProvider(command);
        CliCommand actual = create();

        actual.execute(applicationInstance);

        assertThat(systemOutCaptor.getLog())
            .contains(command.getCode())
            .contains(command.getName())
            .contains(command.getDescription(command, applicationInstance));
    }

    @Override
    @ExtendWith(SystemOutCaptor.class)
    public void execute() {
        CliCommandContract.super.execute();
    }

    @Override
    public HelpCommand create() {
        return new HelpCommand(delegatingCliCommandProvider);
    }

    @Override
    public boolean isExpectedToBeHelpCommand() {
        return true;
    }

    @Override
    public boolean isExpectedToPrintHelpOnStartup() {
        return true;
    }

    @Override
    public void assertDescription(AbstractStringAssert<?> description, CliCommand command, ApplicationInstance applicationInstance) {
        description.contains(command.getCode());
    }

    private void stubCliCommandProvider(TestCliCommand command) {
        when(delegatingCliCommandProvider.getCliCommands()).thenReturn(Set.of(command));
    }
}
