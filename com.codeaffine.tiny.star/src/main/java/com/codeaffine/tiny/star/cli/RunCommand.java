package com.codeaffine.tiny.star.cli;

import static java.lang.String.format;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

class RunCommand implements CliCommand {

    @Override
    public String getCode() {
        return "r";
    }

    @Override
    public String getName() {
        return Texts.RUN_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationServer) {
        return format(Texts.RUN_DESCRIPTION, command.getCode(), applicationServer.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationServer) {
        applicationServer.start();
    }
}
