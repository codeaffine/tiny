package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandProvider;

import java.util.Set;

public class TestCliCommandProvider implements CliCommandProvider {

    @Override
    public Set<CliCommand> getCliCommands() {
        return Set.of(new TestCliCommand());
    }
}
