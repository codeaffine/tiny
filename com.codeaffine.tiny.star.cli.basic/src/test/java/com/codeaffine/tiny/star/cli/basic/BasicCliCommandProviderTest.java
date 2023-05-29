/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.tck.CliCommandProviderContract;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;

import java.util.Collection;

class BasicCliCommandProviderTest implements CliCommandProviderContract<BasicCliCommandProvider> {

    @Override
    public BasicCliCommandProvider create() {
        return new BasicCliCommandProvider();
    }

    @Override
    public void assertProvidedCliCommands(AbstractCollectionAssert<?, Collection<? extends CliCommand>, CliCommand, ObjectAssert<CliCommand>> cliCommands) {
        cliCommands
            .map(command -> command.getClass().getName())
            .containsExactlyInAnyOrder(
                QuitCommand.class.getName(),
                HelpCommand.class.getName(),
                StateCommand.class.getName(),
                RunCommand.class.getName()
            );
    }
}
