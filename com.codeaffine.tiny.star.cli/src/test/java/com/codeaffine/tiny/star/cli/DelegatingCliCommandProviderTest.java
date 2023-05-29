/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.test.SystemPrintStreamCaptor.SystemErrCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemErrCaptor.class)
class DelegatingCliCommandProviderTest {

    @Test
    void getCliCommands(SystemErrCaptor systemErrCaptor) {
        DelegatingCliCommandProvider commandProvider = new DelegatingCliCommandProvider();

        Set<CliCommand> actual = commandProvider.getCliCommands();

        assertThat(actual)
            .hasSize(1)
            .allMatch(command -> command instanceof TestCliCommand);
        assertThat(systemErrCaptor.getLog())
            .contains(TestCliCommandProviderWithServiceConfigurationError.class.getName())
            .contains(TestCliCommandProviderWithRuntimeException.class.getName())
            .contains(TestCliCommandProviderWithRuntimeException.ERROR_MESSAGE);
    }
}
