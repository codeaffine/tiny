package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.cli.Texts.STD_OUT_UNKNOWN_COMMAND;
import static lombok.AccessLevel.PACKAGE;

import static java.util.stream.Collectors.joining;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class CommandDispatcher {

    @NonNull
    private final ApplicationInstance applicationInstance;
    @NonNull
    private final Map<String, CliCommand> codeToCommandMap;
    @NonNull
    private final ExecutorServiceAdapter executor;

    void dispatchCommand(String commandCode) {
        if (codeToCommandMap.containsKey(commandCode)) {
            CliCommand cliCommand = codeToCommandMap.get(commandCode);
            executor.execute(() -> cliCommand.execute(applicationInstance));
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
