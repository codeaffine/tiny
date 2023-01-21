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
