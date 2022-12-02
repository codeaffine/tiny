package com.codeaffine.tiny.star.spi;

import java.util.Set;

public interface CliCommandProvider {
    Set<CliCommand> getCliCommands();
}
