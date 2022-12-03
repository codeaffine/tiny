package com.codeaffine.tiny.star.spi;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

public interface CliCommandProviderContract <T extends CliCommandProvider> {

    T create();

    @Test
    default void getCliCommands() {
        T provider = create();

        Set<CliCommand> actual = provider.getCliCommands();

        AbstractCollectionAssert<?, Collection<? extends CliCommand>, CliCommand, ObjectAssert<CliCommand>> cliCommands = assertThat(actual);
        cliCommands.isNotNull();
        assertProvidedCliCommands(cliCommands);
    }

    default void assertProvidedCliCommands(AbstractCollectionAssert<?, Collection<? extends CliCommand>, CliCommand, ObjectAssert<CliCommand>> cliCommands) {
        // subclasses may override
    }
}
