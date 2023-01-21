package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.CliCommandProviderContract;
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
