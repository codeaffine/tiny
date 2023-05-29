/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.tck.CliCommandContract;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.codeaffine.tiny.test.SystemPrintStreamCaptor.SystemOutCaptor;
import static org.assertj.core.api.Assertions.assertThat;

class HelpCommandTest implements CliCommandContract<HelpCommand> {

    @Test
    @ExtendWith(SystemOutCaptor.class)
    void execute(SystemOutCaptor systemOutCaptor) {
        ApplicationServer applicationServer = CliCommandContract.stubApplicationServer();
        QuitCommand quitCommand = new QuitCommand();
        CliCommand helpCommand = create();
        Map<String, CliCommand> codeToCommandMap = Map.of(quitCommand.getCode(), quitCommand, helpCommand.getCode(), helpCommand);

        helpCommand.execute(applicationServer, codeToCommandMap);

        assertThat(systemOutCaptor.getLog())
            .contains(quitCommand.getCode())
            .contains(quitCommand.getName())
            .contains(quitCommand.getDescription(quitCommand, applicationServer))
            .contains(helpCommand.getCode())
            .contains(helpCommand.getName())
            .contains(helpCommand.getDescription(helpCommand, applicationServer));
    }

    @Override
    @ExtendWith(SystemOutCaptor.class)
    @SuppressWarnings({"EmptyMethod", "unused"})
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
