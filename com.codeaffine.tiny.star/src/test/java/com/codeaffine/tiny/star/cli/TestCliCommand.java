package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;

class TestCliCommand implements CliCommand {

    static final String CODE = "c";
    static final String HELP_TEXT = "help text";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getName() {
        return "Command";
    }

    @Override
    public String getDescription(String code, ApplicationInstance applicationInstance) {
        return HELP_TEXT;
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }

    @Override
    public void execute(ApplicationInstance applicationInstance) {

    }
}
