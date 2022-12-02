package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationInstance.State;
import static com.codeaffine.tiny.star.ApplicationInstance.State.RUNNING;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;

class ShutdownHookHandlerTest {

    @ParameterizedTest
    @CsvSource({
        "STARTING",
        "STOPPING",
        "HALTED"
    })
    void beforeProcessShutdownIfInstanceStateIsNotRunning(State state) {
        Terminator terminator = mock(Terminator.class);
        ApplicationInstance applicationInstance = stubApplicationInstanceGetState(state);

        ShutdownHookHandler.beforeProcessShutdown(terminator, applicationInstance);

        InOrder order = inOrder(terminator, applicationInstance);
        order.verify(terminator).setShutdownHookExecution(true);
        order.verify(applicationInstance).getState();
        order.verify(terminator).deleteWorkingDirectory();
        order.verifyNoMoreInteractions();
    }

    @Test
    void beforeProcessShutdownIfInstanceIsRunning() {
        Terminator terminator = mock(Terminator.class);
        ApplicationInstance applicationInstance = stubApplicationInstanceGetState(RUNNING);

        ShutdownHookHandler.beforeProcessShutdown(terminator, applicationInstance);

        InOrder order = inOrder(terminator, applicationInstance);
        order.verify(terminator).setShutdownHookExecution(true);
        order.verify(applicationInstance).getState();
        order.verify(applicationInstance).stop();
        order.verifyNoMoreInteractions();
    }

    @Test
    void beforeProcessShutdownWithNullAsTerminatorArgument() {
        assertThatThrownBy(() -> ShutdownHookHandler.beforeProcessShutdown(null, mock(ApplicationInstance.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void beforeProcessShutdownWithNullAsApplicationInstanceArgument() {
        assertThatThrownBy(() -> ShutdownHookHandler.beforeProcessShutdown(mock(Terminator.class), null))
            .isInstanceOf(NullPointerException.class);
    }

    private static ApplicationInstance stubApplicationInstanceGetState(State state) {
        ApplicationInstance result = mock(ApplicationInstance.class);
        when(result.getState()).thenReturn(state);
        return result;
    }
}
