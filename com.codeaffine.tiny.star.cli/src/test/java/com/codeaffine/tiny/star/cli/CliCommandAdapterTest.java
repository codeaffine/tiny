package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class CliCommandAdapterTest {

    private static final int CLI_INSTANCE_NUMBER = 1;
    public static final String CODE = "C";
    public static final int ZERO = 0;

    private ApplicationServer applicationServer;
    private CliCommand command;

    @BeforeEach
    void setUp() {
        applicationServer = mock(ApplicationServer.class);
        command = stubCommand();
    }

    @Test
    void getCode() {
        CliCommandAdapter adapter = new CliCommandAdapter(applicationServer, command, CLI_INSTANCE_NUMBER);

        String actual = adapter.getCode();

        assertThat(actual).isEqualTo(CODE + CLI_INSTANCE_NUMBER);
    }

    @Test
    void getCodeWithZeroAsCliInstanceNumberArgument() {
        CliCommandAdapter adapter = new CliCommandAdapter(applicationServer, command, ZERO);

        String actual = adapter.getCode();

        assertThat(actual).isEqualTo(CODE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute() {
        CliCommandAdapter adapter = new CliCommandAdapter(applicationServer, command, CLI_INSTANCE_NUMBER);
        Map<String, CliCommand> codeToCommandMap = new HashMap<>(Map.of(adapter.getCode(), adapter));
        Map<String, CliCommand> copyOfCodeToCommandMap = copyAndClearMap(codeToCommandMap);

        adapter.execute(null, copyOfCodeToCommandMap);

        ArgumentCaptor<Map<String, CliCommand>> codeToCommandMapCaptor = forClass(Map.class);
        verify(command).execute(eq(applicationServer), codeToCommandMapCaptor.capture());
        Map<String, CliCommand> copyOfCapturedCodeToCommandMap = copyAndClearMap(codeToCommandMapCaptor.getValue());
        assertThat(copyOfCapturedCodeToCommandMap)
            .containsExactlyEntriesOf(copyOfCodeToCommandMap)
            .containsEntry(CODE + CLI_INSTANCE_NUMBER, adapter);
        assertThat(codeToCommandMapCaptor.getValue())
            .isNotSameAs(copyOfCodeToCommandMap);
    }

    @Test
    void constructWithNullAsApplicationServerArgument() {
        assertThatThrownBy(() -> new CliCommandAdapter(null, command, CLI_INSTANCE_NUMBER))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsDelegateArgument() {
        assertThatThrownBy(() -> new CliCommandAdapter(applicationServer, null, CLI_INSTANCE_NUMBER))
            .isInstanceOf(NullPointerException.class);
    }

    private static CliCommand stubCommand() {
        CliCommand result = mock(CliCommand.class);
        when(result.getCode()).thenReturn(CODE);
        return result;
    }

    private static Map<String, CliCommand> copyAndClearMap(Map<String, CliCommand> codeToCommandMap) {
        Map<String, CliCommand> result = new HashMap<>(codeToCommandMap);
        codeToCommandMap.clear();
        return result;
    }
}
