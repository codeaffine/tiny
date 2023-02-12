package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.tck.CliCommandContract;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RunCommandTest implements CliCommandContract<RunCommand> {

    @Override
    public RunCommand create() {
        return new RunCommand();
    }

    @Test
    void executeWithApplicationServerArgument() {
        RunCommand command = create();
        ApplicationServer applicationServer = mock(ApplicationServer.class);

        command.execute(applicationServer);

        verify(applicationServer).start();
    }

    @Test
    void executeWithNullAsApplicationServerArgument() {
        RunCommand actual = create();

        assertThatThrownBy(() -> actual.execute(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsCommandArgument() {
        ApplicationServer serverInstance = CliCommandContract.stubApplicationServer();
        RunCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(null, serverInstance))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDescriptionWithNullAsApplicationServerArgument() {
        RunCommand actual = create();

        assertThatThrownBy(() -> actual.getDescription(actual, null))
            .isInstanceOf(NullPointerException.class);
    }
}
