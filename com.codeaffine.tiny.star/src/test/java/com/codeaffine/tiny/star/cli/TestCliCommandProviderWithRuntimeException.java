package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandProvider;

import java.util.Set;

public class TestCliCommandProviderWithRuntimeException implements CliCommandProvider {

    static final String ERROR_MESSAGE = "Problem executing getCliCommands.";

    @Override
    public Set<CliCommand> getCliCommands() {
        throw new RuntimeException(ERROR_MESSAGE);
    }
}
