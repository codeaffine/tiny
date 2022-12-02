package com.codeaffine.tiny.star.cli;

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
        return "Quit";
    }

    @Override
    public String getDescription(String code, ApplicationInstance applicationInstance) {
        return format("Type %s to stop %s instance.", code, applicationInstance.getIdentifier());
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }

    @Override
    public void execute(ApplicationInstance applicationInstance) {
        applicationInstance.stop();
    }
}
