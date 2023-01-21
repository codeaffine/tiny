package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;

import static java.lang.String.format;

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
    public String getDescription(CliCommand command, ApplicationServer applicationServer) {
        return format("%s %s", command.getCode(), applicationServer.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationServer) {
        applicationServer.stop();
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
