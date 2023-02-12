package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.CliCommandProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.Set;

import static java.util.ServiceLoader.load;

class DelegatingCliCommandProvider implements CliCommandProvider {

    @Override
    public Set<CliCommand> getCliCommands() {
        Iterator<CliCommandProvider> providers = load(CliCommandProvider.class).iterator();
        Set<CliCommand> result = new HashSet<>();
        while (providers.hasNext()) {
            loadAndAdd(providers, result);
        }
        return result;
    }

    private static void loadAndAdd(Iterator<CliCommandProvider> providers, Set<CliCommand> result) {
        try {
            doLoadAndAdd(providers, result);
        } catch(Exception | ServiceConfigurationError cause) {
            cause.printStackTrace();
        }
    }

    private static void doLoadAndAdd(Iterator<CliCommandProvider> providers, Set<CliCommand> result) {
        CliCommandProvider provider = providers.next();
        result.addAll(provider.getCliCommands());
    }
}
