package com.codeaffine.tiny.star.cli;

import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;

import com.codeaffine.tiny.star.spi.CliCommand;
import com.codeaffine.tiny.star.spi.CliCommandProviderContract;

import java.util.Collection;

class CoreCliCommandProviderTest implements CliCommandProviderContract<CoreCliCommandProvider> {

    @Override
    public CoreCliCommandProvider create() {
        return new CoreCliCommandProvider();
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
