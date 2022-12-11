package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.cli.Texts.QUIT_DESCRIPTION;
import static com.codeaffine.tiny.star.cli.Texts.QUIT_NAME;

import static java.lang.String.format;

import com.codeaffine.tiny.star.ApplicationServer;
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
    public String getDescription(CliCommand command, ApplicationServer applicationInstance) {
        return format(QUIT_DESCRIPTION, command.getCode(), applicationInstance.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationInstance) {
        applicationInstance.stop();
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }
}
