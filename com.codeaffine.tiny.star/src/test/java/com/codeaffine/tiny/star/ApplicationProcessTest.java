package com.codeaffine.tiny.star;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import java.util.stream.Stream;

import static com.codeaffine.tiny.star.ApplicationProcess.LifecycleException;
import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.State.*;
import static com.codeaffine.tiny.star.Texts.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationProcessTest {

    private static final String IDENTIFIER = "identifier";

    private LifecycleConsumerListener lifecycleConsumingListener;
    private ParameterlessListener parameterlessListener;
    private ApplicationProcess applicationProcess;
    private ApplicationServer applicationServer;
    private Runnable terminator;
    private Runnable starter;
    private Logger logger;

    interface LifecycleConsumerListener {
        @Starting
        void starting(ApplicationServer applicationServer);
        @Started
        void started(ApplicationServer applicationServer);
        @Stopping
        void stopping(ApplicationServer applicationServer);
        @Stopped
        void stopped(ApplicationServer applicationServer);
    }

    interface ParameterlessListener {
        @Starting
        void starting();
        @Started
        void started();
        @Stopping
        void stopping();
        @Stopped
        void stopped();
    }

    @BeforeEach
    void setUp() {
        terminator = mock(Runnable.class);
        starter = mock(Runnable.class);
        logger = mock(Logger.class);
        applicationServer = stubApplicationServer();
        applicationProcess = new ApplicationProcess(applicationServer, starter, terminator, logger);
        stubApplicationServerGetState();
        lifecycleConsumingListener = mock(LifecycleConsumerListener.class);
        parameterlessListener = mock(ParameterlessListener.class);
        applicationProcess.registerLifecycleListener(lifecycleConsumingListener);
        applicationProcess.registerLifecycleListener(parameterlessListener);
    }

    @Test
    void start() {
        StateCaptor stateCaptor = new StateCaptor();
        applicationProcess.registerLifecycleListener(stateCaptor);

        State beforeState = applicationProcess.getState();
        applicationProcess.start();
        State afterState = applicationProcess.getState();

        assertThat(beforeState).isSameAs(HALTED);
        assertThat(afterState).isSameAs(RUNNING);
        assertThat(stateCaptor.getHalted()).isNull();
        assertThat(stateCaptor.getStarting()).isSameAs(STARTING);
        assertThat(stateCaptor.getStarted()).isSameAs(STARTING);
        assertThat(stateCaptor.getStopping()).isNull();
        verifyStartProcedure();
    }

    @Test
    void startIfRunning() {
        applicationProcess.start();
        reset(starter, lifecycleConsumingListener, parameterlessListener);

        State beforeState = applicationProcess.getState();
        applicationProcess.start();
        State afterState = applicationProcess.getState();

        verify(lifecycleConsumingListener, never()).starting(applicationServer);
        verify(parameterlessListener, never()).starting();
        verify(starter, never()).run();
        verify(lifecycleConsumingListener, never()).started(applicationServer);
        verify(parameterlessListener, never()).started();
        verify(logger).debug(DEBUG_APPLICATION_NOT_HALTED);
        assertThat(afterState)
            .isSameAs(beforeState)
            .isSameAs(RUNNING);
    }

    @Test
    void startOnStarting() {
        startOn().starting(applicationServer);

        applicationProcess.start();

        verifyStartProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startOnStarted() {
        startOn().started(applicationServer);

        applicationProcess.start();

        verifyStartProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startOnStopping() {
        applicationProcess.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        startOn().stopping(applicationServer);

        applicationProcess.stop();

        verifyStopProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startOnStopped() {
        applicationProcess.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        startOn().stopped(applicationServer);

        applicationProcess.stop();

        verifyStopProcedure();
        verify(logger, never()).debug(DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startWithStartingListenerThrowingException() {
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).starting();

        Throwable actual = catchThrowable(() -> applicationProcess.start());

        verify(lifecycleConsumingListener).starting(applicationServer);
        verify(starter, never()).run();
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void startWithStartedListenerThrowingException() {
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).started();

        Throwable actual = catchThrowable(() -> applicationProcess.start());

        InOrder order = inOrder(starter, terminator, lifecycleConsumingListener, parameterlessListener);
        order.verify(lifecycleConsumingListener).starting(applicationServer);
        order.verify(parameterlessListener).starting();
        order.verify(starter).run();
        order.verify(lifecycleConsumingListener).started(applicationServer);
        order.verify(lifecycleConsumingListener).stopping(applicationServer);
        order.verify(parameterlessListener).stopping();
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationServer);
        order.verify(parameterlessListener).stopped();
        assertThat(actual).isSameAs(expected);
        verify(logger).error(ERROR_NOTIFYING_STARTED_LISTENER, expected);
        verify(logger).error(ENFORCING_APPLICATION_TERMINATION);
    }

    @Test
    void startWithStartedListenerAndStoppingThrowingException() {
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).started();
        throwGivenExceptionOnListenerMethod(expected).stopping();

        Throwable actual = catchThrowable(() -> applicationProcess.start());

        InOrder order = inOrder(starter, terminator, lifecycleConsumingListener, parameterlessListener, logger);
        order.verify(lifecycleConsumingListener).starting(applicationServer);
        order.verify(parameterlessListener).starting();
        order.verify(starter).run();
        order.verify(lifecycleConsumingListener).started(applicationServer);
        order.verify(lifecycleConsumingListener).stopping(applicationServer);
        order.verify(logger).error(ERROR_NOTIFYING_STOPPING_LISTENER, expected);
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationServer);
        order.verify(parameterlessListener).stopped();
        verify(logger).error(ERROR_NOTIFYING_STARTED_LISTENER, expected);
        verify(logger).error(ENFORCING_APPLICATION_TERMINATION);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void stop() {
        StateCaptor stateCaptor = new StateCaptor();
        applicationProcess.start();
        applicationProcess.registerLifecycleListener(stateCaptor);
        reset(terminator, lifecycleConsumingListener, parameterlessListener);

        State beforeState = applicationProcess.getState();
        applicationProcess.stop();
        State afterState = applicationProcess.getState();

        assertThat(beforeState).isSameAs(RUNNING);
        assertThat(stateCaptor.getStarted()).isNull();
        assertThat(stateCaptor.getStopping()).isSameAs(STOPPING);
        assertThat(stateCaptor.getHalted()).isSameAs(HALTED);
        assertThat(stateCaptor.getStarting()).isNull();
        assertThat(afterState).isSameAs(HALTED);
        verifyStopProcedure();
    }

    @Test
    void stopIfHalted() {
        State beforeState = applicationProcess.getState();
        applicationProcess.stop();
        State afterState = applicationProcess.getState();

        verify(lifecycleConsumingListener, never()).stopping(applicationServer);
        verify(parameterlessListener, never()).stopping();
        verify(terminator, never()).run();
        verify(lifecycleConsumingListener, never()).stopped(applicationServer);
        verify(parameterlessListener, never()).stopped();
        verify(logger).debug(DEBUG_APPLICATION_NOT_RUNNING);
        assertThat(beforeState)
            .isSameAs(afterState)
            .isSameAs(HALTED);
    }

    @Test
    void stopOnStopping() {
        applicationProcess.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        stopOn().stopping(applicationServer);

        applicationProcess.stop();

        verifyStopProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopOnStopped() {
        applicationProcess.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        stopOn().stopped(applicationServer);

        applicationProcess.stop();

        verifyStopProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopOnStarting() {
        stopOn().starting(applicationServer);

        applicationProcess.start();

        verifyStartProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopOnStarted() {
        stopOn().started(applicationServer);

        applicationProcess.start();

        verifyStartProcedure();
        verify(logger).debug(DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopWithStoppingListenerThrowingException() {
        applicationProcess.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).stopping();

        Throwable actual = catchThrowable(() -> applicationProcess.stop());

        InOrder order = inOrder(terminator, lifecycleConsumingListener, parameterlessListener, logger);
        order.verify(lifecycleConsumingListener).stopping(applicationServer);
        order.verify(logger).error(ERROR_NOTIFYING_STOPPING_LISTENER, expected);
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationServer);
        order.verify(parameterlessListener).stopped();
        order.verifyNoMoreInteractions();
        assertThat(actual)
            .isInstanceOf(LifecycleException.class)
            .hasMessage(ERROR_TERMINATING_APPLICATION);
    }

    @Test
    void stopWithStoppedListenerThrowingException() {
        applicationProcess.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).stopped();

        Throwable actual = catchThrowable(() -> applicationProcess.stop());

        InOrder order = inOrder(terminator, lifecycleConsumingListener, parameterlessListener, logger);
        order.verify(lifecycleConsumingListener).stopping(applicationServer);
        order.verify(parameterlessListener).stopping();
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationServer);
        order.verify(logger).error(ERROR_NOTIFYING_STOPPED_LISTENER, expected);
        order.verifyNoMoreInteractions();
        assertThat(actual)
            .isInstanceOf(LifecycleException.class)
            .hasMessage(ERROR_TERMINATING_APPLICATION);
    }

    @ParameterizedTest
    @MethodSource("provideLifecycleListenersWithIllegalSignature")
    void registerLifecycleListenerWithIllegalSignature(Object listenerWithIllegalMethodSignature) {
        Throwable actual = catchThrowable(() -> applicationProcess.registerLifecycleListener(listenerWithIllegalMethodSignature));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(listenerWithIllegalMethodSignature.getClass().getName())
            .hasMessageContaining("starting")
            .hasMessageContaining(ApplicationProcess.class.getName());
    }

    @Test
    void deregisterLifecycleListener() {
        applicationProcess.deregisterLifecycleListener(lifecycleConsumingListener);

        applicationProcess.start();

        InOrder order = inOrder(lifecycleConsumingListener, parameterlessListener);
        order.verify(parameterlessListener).starting();
        order.verify(parameterlessListener).started();
        order.verifyNoMoreInteractions();
    }

    @Test
    void constructWithNullAsNameArgument() {
        assertThatThrownBy(() -> new ApplicationProcess(null, starter, terminator, logger))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsStarterArgument() {
        assertThatThrownBy(() -> new ApplicationProcess(applicationServer, null, terminator, logger))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsTerminatorArgument() {
        assertThatThrownBy(() -> new ApplicationProcess(applicationServer, starter, null, logger))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsLoggerArgument() {
        assertThatThrownBy(() -> new ApplicationProcess(applicationServer, starter, terminator, null))
            .isInstanceOf(NullPointerException.class);
    }

    private void verifyStartProcedure() {
        InOrder order = inOrder(starter, lifecycleConsumingListener, parameterlessListener);
        order.verify(lifecycleConsumingListener).starting(applicationServer);
        order.verify(parameterlessListener).starting();
        order.verify(starter).run();
        order.verify(lifecycleConsumingListener).started(applicationServer);
        order.verify(parameterlessListener).started();
        order.verifyNoMoreInteractions();
    }

    private void verifyStopProcedure() {
        InOrder order = inOrder(terminator, lifecycleConsumingListener, parameterlessListener);
        order.verify(lifecycleConsumingListener).stopping(applicationServer);
        order.verify(parameterlessListener).stopping();
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationServer);
        order.verify(parameterlessListener).stopped();
        order.verifyNoMoreInteractions();
    }

    static Stream<Object> provideLifecycleListenersWithIllegalSignature() {
        return Stream.of(
            new Object() {
                @Starting
                @SuppressWarnings({"EmptyMethod", "unused"})
                void starting(@SuppressWarnings("unused") String parameter) {
                }
            },
            new Object() {
                @Starting
                @SuppressWarnings({"EmptyMethod", "unused"})
                void starting(@SuppressWarnings("unused") ApplicationProcess applicationProcess, @SuppressWarnings("unused") String parameter) {
                }
            }
        );
    }

    private ParameterlessListener throwGivenExceptionOnListenerMethod(Exception exception) {
        return doThrow(exception).when(parameterlessListener);
    }

    private LifecycleConsumerListener startOn() {
        return doAnswer(invocation -> triggerStart())
            .when(lifecycleConsumingListener);
    }

    private Void triggerStart() {
        applicationProcess.start();
        return null;
    }

    private LifecycleConsumerListener stopOn() {
        return doAnswer(invocation -> triggerStop())
            .when(lifecycleConsumingListener);
    }

    private Void triggerStop() {
        applicationProcess.stop();
        return null;
    }

    private static ApplicationServer stubApplicationServer() {
        ApplicationServer result = mock(ApplicationServer.class);
        when(result.getIdentifier()).thenReturn(IDENTIFIER);
        return result;
    }

    private void stubApplicationServerGetState() {
        when(applicationServer.getState()).thenAnswer((Answer<State>) invocation -> applicationProcess.getState());
    }
}
