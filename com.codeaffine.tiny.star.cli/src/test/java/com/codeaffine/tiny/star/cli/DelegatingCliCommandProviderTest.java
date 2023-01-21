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
