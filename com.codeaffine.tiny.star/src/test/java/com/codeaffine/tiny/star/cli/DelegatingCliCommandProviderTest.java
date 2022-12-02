package com.codeaffine.tiny.star.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemErrCaptor;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Set;

@ExtendWith(SystemErrCaptor.class)
class DelegatingCliCommandProviderTest {

    @Test
    void getCliCommands(SystemErrCaptor systemErrCaptor) {
        DelegatingCliCommandProvider commandProvider = new DelegatingCliCommandProvider();

        Set<CliCommand> actual = commandProvider.getCliCommands();

        assertThat(actual)
            .map(command -> command.getClass().getName())
            .contains(TestCliCommand.class.getName(), QuitCommand.class.getName());
        assertThat(systemErrCaptor.getLog())
            .contains(TestCliCommandProviderWithServiceConfigurationError.class.getName())
            .contains(TestCliCommandProviderWithRuntimeException.class.getName())
            .contains(TestCliCommandProviderWithRuntimeException.ERROR_MESSAGE);
    }
}
