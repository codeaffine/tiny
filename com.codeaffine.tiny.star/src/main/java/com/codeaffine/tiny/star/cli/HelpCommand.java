package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.cli.Texts.HELP_DESCRIPTION;
import static com.codeaffine.tiny.star.cli.Texts.HELP_NAME;
import static com.codeaffine.tiny.star.cli.Texts.STD_OUT_AVAILABLE_COMMANDS_DESCRIPTION;
import static lombok.AccessLevel.PACKAGE;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Map;
import lombok.RequiredArgsConstructor;

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
