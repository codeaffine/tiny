package com.codeaffine.tiny.star.cli;

import static java.util.Collections.emptySet;

import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandProvider;

import java.util.Set;

// only public classes with public default constructors are allowed as service providers.
// Hence, loading this class should throw an exception
class TestCliCommandProviderWithServiceConfigurationError implements CliCommandProvider {

    @Override
    public Set<CliCommand> getCliCommands() {
        return emptySet();
    }
}
