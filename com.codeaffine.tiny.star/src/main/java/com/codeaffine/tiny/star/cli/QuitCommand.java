package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.cli.Texts.*;

import static java.lang.String.format;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;

class QuitCommand implements CliCommand {

    @Override
    public String getCode() {
        return "q";
    }

    @Override
    public String getName() {
        return QUIT_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationInstance applicationInstance) {
        return format(QUIT_DESCRIPTION, command.getCode(), applicationInstance.getIdentifier());
    }

    @Override
    public void execute(ApplicationInstance applicationInstance) {
        applicationInstance.stop();
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }
}
