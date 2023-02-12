package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.tck.CliCommandContract;
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
