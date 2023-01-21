package com.codeaffine.tiny.star.cli.spi;

import java.util.Set;

public interface CliCommandProvider {
    Set<CliCommand> getCliCommands();
}
