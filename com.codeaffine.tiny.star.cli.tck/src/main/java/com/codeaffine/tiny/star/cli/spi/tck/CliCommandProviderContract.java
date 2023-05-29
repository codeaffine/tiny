/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.spi.tck;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.CliCommandProvider;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public interface CliCommandProviderContract <T extends CliCommandProvider> {

    T create();

    @Test
    default void getCliCommands() {
        T provider = create();

        Set<CliCommand> actual = provider.getCliCommands();

        AbstractCollectionAssert<?, Collection<? extends CliCommand>, CliCommand, ObjectAssert<CliCommand>> cliCommands = assertThat(actual);  // NOSONAR
        cliCommands.isNotNull();
        assertProvidedCliCommands(cliCommands);
    }

    default void assertProvidedCliCommands(AbstractCollectionAssert<?, Collection<? extends CliCommand>, CliCommand, ObjectAssert<CliCommand>> cliCommands) {
        // subclasses may override
    }
}
