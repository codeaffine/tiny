package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.CliCommandProvider;

import java.util.Set;

public class BasicCliCommandProvider implements CliCommandProvider {

    @Override
    public Set<CliCommand> getCliCommands() {
        return Set.of(new QuitCommand(), new HelpCommand(), new StateCommand(), new RunCommand());
    }
}
