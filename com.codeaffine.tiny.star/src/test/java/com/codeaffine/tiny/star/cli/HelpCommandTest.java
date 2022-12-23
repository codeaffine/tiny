package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemOutCaptor;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandContract;

import java.util.Map;

class HelpCommandTest implements CliCommandContract<HelpCommand> {

    @Test
    @ExtendWith(SystemOutCaptor.class)
    void execute(SystemOutCaptor systemOutCaptor) {
        ApplicationServer applicationServer = CliCommandContract.stubApplicationServer();
        TestCliCommand testCommand = new TestCliCommand();
        CliCommand helpCommand = create();
        Map<String, CliCommand> codeToCommandMap = Map.of(testCommand.getCode(), testCommand, helpCommand.getCode(), helpCommand);

        helpCommand.execute(applicationServer, codeToCommandMap);

        assertThat(systemOutCaptor.getLog())
            .contains(testCommand.getCode())
            .contains(testCommand.getName())
            .contains(testCommand.getDescription(testCommand, applicationServer))
            .contains(helpCommand.getCode())
            .contains(helpCommand.getName())
            .contains(helpCommand.getDescription(helpCommand, applicationServer));
    }

    @Override
    @ExtendWith(SystemOutCaptor.class)
    public void execute() {
        CliCommandContract.super.execute();
    }

    @Override
    public HelpCommand create() {
        return new HelpCommand();
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
    public void assertDescription(AbstractStringAssert<?> description, CliCommand command, ApplicationServer applicationServer) {
        description.contains(command.getCode());
    }
}
