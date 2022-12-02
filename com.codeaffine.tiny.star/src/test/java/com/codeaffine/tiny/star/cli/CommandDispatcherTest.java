package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemOutCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.Collections.emptyMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Map;

class CommandDispatcherTest {

    private static final String CODE = "code";
    private static final String UNKNOWN_COMMAND_CODE = "unknown-command-code";

    private ApplicationInstance applicationInstance;
    private ExecutorServiceAdapter executor;
    private CommandDispatcher dispatcher;
    private CliCommand command;

    @BeforeEach
    void setUp() {
        applicationInstance = mock(ApplicationInstance.class);
        command = mock(CliCommand.class);
        executor = mock(ExecutorServiceAdapter.class);
        dispatcher = new CommandDispatcher(applicationInstance, Map.of(CODE, command), executor);
    }

    @Test
    void dispatchCommand() {
        dispatcher.dispatchCommand(CODE);

        captureCommandAndExecute();
        verify(command).execute(applicationInstance);
    }

    @Test
    @ExtendWith(SystemOutCaptor.class)
    void dispatchCommandWithUnknownCode(SystemOutCaptor systemOutCaptor) {
        when(command.isHelpCommand()).thenReturn(true);

        dispatcher.dispatchCommand(UNKNOWN_COMMAND_CODE);

        assertThat(systemOutCaptor.getLog())
            .contains(UNKNOWN_COMMAND_CODE)
            .contains(CODE);
    }

    private void captureCommandAndExecute() {
        ArgumentCaptor<Runnable> commandCaptor = forClass(Runnable.class);
        verify(executor).execute(commandCaptor.capture());
        commandCaptor.getValue().run();
    }

    @Test
    void constructWithNullAsApplicationInstanceArgument() {
        Map<String, CliCommand> emptyCodeToCommandMap = emptyMap();

        assertThatThrownBy(() -> new CommandDispatcher(null, emptyCodeToCommandMap, executor))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsCodeToCommandMapArgument() {
        assertThatThrownBy(() -> new CommandDispatcher(applicationInstance, null, executor))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsExecutorArgument() {
        Map<String, CliCommand> emptyCodeToCommandMap = emptyMap();

        assertThatThrownBy(() -> new CommandDispatcher(applicationInstance, emptyCodeToCommandMap, null))
            .isInstanceOf(NullPointerException.class);
    }
}
