/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static com.codeaffine.tiny.test.SystemPrintStreamCaptor.SystemOutCaptor;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class CommandDispatcherTest {

    private static final String UNKNOWN_COMMAND_CODE = "unknown-command-code";
    private static final String CODE = "code";

    private Map<String, CliCommand> codeToCommandMap;
    private ExecutorServiceAdapter executor;
    private CommandDispatcher dispatcher;
    private CliCommand command;

    @BeforeEach
    void setUp() {
        command = mock(CliCommand.class);
        executor = mock(ExecutorServiceAdapter.class);
        codeToCommandMap = Map.of(CODE, command);
        dispatcher = new CommandDispatcher(codeToCommandMap, executor);
    }

    @Test
    void dispatchCommand() {
        dispatcher.dispatchCommand(CODE);

        captureCommandAndExecute();
        verify(command).execute(null, codeToCommandMap);
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
    void constructWithNullAsCodeToCommandMapArgument() {
        assertThatThrownBy(() -> new CommandDispatcher(null, executor))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsExecutorArgument() {
        Map<String, CliCommand> emptyCodeToCommandMap = emptyMap();

        assertThatThrownBy(() -> new CommandDispatcher(emptyCodeToCommandMap, null))
            .isInstanceOf(NullPointerException.class);
    }
}
