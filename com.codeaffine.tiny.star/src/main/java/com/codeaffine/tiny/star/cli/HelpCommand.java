package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

import org.slf4j.Logger;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Comparator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class HelpCommand implements CliCommand {

    @NonNull
    private final DelegatingCliCommandProvider commandProvider;
    @NonNull
    private final Logger logger;

    HelpCommand() {
        this(new DelegatingCliCommandProvider(), getLogger(HelpCommand.class));
    }

    @Override
    public String getCode() {
        return "h";
    }

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public String getDescription(String code, ApplicationInstance applicationInstance) {
        return format("Type %s to show a description of available commands.", code);
    }

    @Override
    public void execute(ApplicationInstance applicationInstance) {
        System.out.println("Available commands:\n(name [keycode]: description)\n"); // NOSONAR: answers to help requests are intentionally written to stdout
        commandProvider.getCliCommands()
            .stream()
            .sorted(comparing(CliCommand::getName))
            .forEach(command -> {
                System.out.printf("%s [%s]%n  %s%n",command.getName(), command.getCode(), command.getDescription(command.getCode(), applicationInstance)); // NOSONAR: answers to help requests are intentionally written to stdout
            });
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }

    @Override
    public boolean isHelpCommand() {
        return true;
    }
}
