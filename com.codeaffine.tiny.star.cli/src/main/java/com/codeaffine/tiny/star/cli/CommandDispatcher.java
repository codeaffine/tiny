/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.codeaffine.tiny.star.cli.Texts.STD_OUT_UNKNOWN_COMMAND;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class CommandDispatcher {

    @NonNull
    private final Map<String, CliCommand> codeToCommandMap;
    @NonNull
    private final ExecutorServiceAdapter executor;

    void dispatchCommand(String commandCode) {
        if (codeToCommandMap.containsKey(commandCode)) {
            CliCommand cliCommand = codeToCommandMap.get(commandCode);
            executor.execute(() -> cliCommand.execute(null, codeToCommandMap));
        } else {
            String helpCommandCodes = extractHelpCommandCodes();
            System.out.printf(STD_OUT_UNKNOWN_COMMAND, commandCode, helpCommandCodes); // NOSONAR
        }
    }

    private String extractHelpCommandCodes() {
        return codeToCommandMap.values()
            .stream()
            .filter(CliCommand::isHelpCommand)
            .map(CliCommand::getCode)
            .collect(joining(","));
    }
}
