package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemErrCaptor;
import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandProviderContract;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemErrCaptor.class)
class DelegatingCliCommandProviderTest implements CliCommandProviderContract<DelegatingCliCommandProvider> {

    @Test
    void getCliCommands(SystemErrCaptor systemErrCaptor) {
        DelegatingCliCommandProvider commandProvider = create();

        Set<CliCommand> actual = commandProvider.getCliCommands();

        assertThat(actual).isNotEmpty();
        assertThat(systemErrCaptor.getLog())
            .contains(TestCliCommandProviderWithServiceConfigurationError.class.getName())
            .contains(TestCliCommandProviderWithRuntimeException.class.getName())
            .contains(TestCliCommandProviderWithRuntimeException.ERROR_MESSAGE);
    }

    @Override
    public DelegatingCliCommandProvider create() {
        return new DelegatingCliCommandProvider();
    }

    @Override
    public void assertProvidedCliCommands(AbstractCollectionAssert<?, Collection<? extends CliCommand>, CliCommand, ObjectAssert<CliCommand>> cliCommands) {
        cliCommands
            .map(command -> command.getClass().getName())
            .containsExactlyInAnyOrder(getExpectedCommandClassNames());
    }

    private static String[] getExpectedCommandClassNames() {
        CoreCliCommandProvider coreCliCommandProvider = new CoreCliCommandProvider();
        Set<CliCommand> expectedCommands = new HashSet<>(coreCliCommandProvider.getCliCommands());
        expectedCommands.add(new TestCliCommand());
        return expectedCommands.stream()
            .map(command -> command.getClass().getName())
            .toArray(String[]::new);
    }
}
