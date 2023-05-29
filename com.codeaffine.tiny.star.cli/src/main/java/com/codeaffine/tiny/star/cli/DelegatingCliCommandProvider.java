/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
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
