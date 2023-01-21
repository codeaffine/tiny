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
