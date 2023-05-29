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
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.codeaffine.tiny.star.cli.basic.Texts.*;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class HelpCommand implements CliCommand {

    @Override
    public String getCode() {
        return "h";
    }

    @Override
    public String getName() {
        return HELP_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationServer) {
        return format(HELP_DESCRIPTION, command.getCode());
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }

    @Override
    public boolean isHelpCommand() {
        return true;
    }

    @Override
    public void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap) {
        printHelpHeader();
        codeToCommandMap.values()
            .stream()
            .sorted(comparing(CliCommand::getName))
            .forEach(command -> printCommandSection(applicationServer, command));
    }

    private static void printHelpHeader() {
        System.out.println(STD_OUT_AVAILABLE_COMMANDS_DESCRIPTION); // NOSONAR: answers to help requests are intentionally written to stdout
    }

    private static void printCommandSection(ApplicationServer applicationServer, CliCommand command) {
        String name = command.getName();
        String code = command.getCode();
        String description = command.getDescription(command, applicationServer);
        System.out.printf("%s [%s]:%n  %s%n", name, code, description); // NOSONAR: answers to help requests are intentionally written to stdout
    }
}
