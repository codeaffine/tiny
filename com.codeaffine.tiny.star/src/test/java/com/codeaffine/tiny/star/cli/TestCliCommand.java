package com.codeaffine.tiny.star.cli;

import static java.lang.String.format;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;

class TestCliCommand implements CliCommand {

    static final String COMMAND = "Command";
    static final String CODE = "c";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getName() {
        return COMMAND;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationInstance applicationInstance) {
        return format("%s %s", command.getCode(), applicationInstance.getIdentifier());
    }

    @Override
    public void execute(ApplicationInstance applicationInstance) {
        applicationInstance.stop();
    }

    @Override
    public boolean isHelpCommand() {
        return true;
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }
}
