package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.cli.Texts.*;
import static com.codeaffine.tiny.star.cli.Texts.STATE_DESCRIPTION;

import static java.lang.String.format;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

public class StateCommand implements CliCommand {

    @Override
    public String getCode() {
        return "s";
    }

    @Override
    public String getName() {
        return STATE_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationInstance) {
        return format(STATE_DESCRIPTION, command.getCode(), applicationInstance.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationInstance) {
        System.out.printf(STD_OUT_STATE_INFO, applicationInstance.getIdentifier(), applicationInstance.getState()); // NOSONAR: answers to state requests are intentionally written to stdout
    }
}
