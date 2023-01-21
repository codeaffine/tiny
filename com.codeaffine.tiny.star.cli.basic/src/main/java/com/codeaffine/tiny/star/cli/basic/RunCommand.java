package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;

import static com.codeaffine.tiny.star.cli.basic.Texts.RUN_DESCRIPTION;
import static com.codeaffine.tiny.star.cli.basic.Texts.RUN_NAME;
import static java.lang.String.format;

class RunCommand implements CliCommand {

    @Override
    public String getCode() {
        return "r";
    }

    @Override
    public String getName() {
        return RUN_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationServer) {
        return format(RUN_DESCRIPTION, command.getCode(), applicationServer.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationServer) {
        applicationServer.start();
    }
}
