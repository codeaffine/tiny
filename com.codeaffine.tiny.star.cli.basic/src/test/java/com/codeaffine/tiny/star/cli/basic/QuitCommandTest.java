/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.test.fixtures.CliCommandContract;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QuitCommandTest implements CliCommandContract<QuitCommand> {

    @Override
    public QuitCommand create() {
        return new QuitCommand();
    }

    @Override
    public boolean isExpectedToPrintHelpOnStartup() {
        return true;
    }

    @Test
    void executeWithApplicationServerArgument() {
        QuitCommand quitCommand = create();
        ApplicationServer applicationServer = mock(ApplicationServer.class);

        quitCommand.execute(applicationServer);

        verify(applicationServer).stop();
    }

    @Test
    void executeWithNullAsApplicationServerArgument() {
        QuitCommand actual = create();
        
        assertThatThrownBy(() -> actual.execute(null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    void getDescriptionWithNullAsCommandArgument() {
        ApplicationServer serverInstance = CliCommandContract.stubApplicationServer();
        QuitCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(null, serverInstance))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsApplicationServerArgument() {
        QuitCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(actual, null))
            .isInstanceOf(NullPointerException.class);
    }
}
