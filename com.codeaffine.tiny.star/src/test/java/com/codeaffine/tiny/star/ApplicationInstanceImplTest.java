package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationInstance.Started;
import static com.codeaffine.tiny.star.ApplicationInstance.Starting;
import static com.codeaffine.tiny.star.ApplicationInstance.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationInstance.State.RUNNING;
import static com.codeaffine.tiny.star.ApplicationInstance.State.STARTING;
import static com.codeaffine.tiny.star.ApplicationInstance.State.STOPPING;
import static com.codeaffine.tiny.star.ApplicationInstance.Stopped;
import static com.codeaffine.tiny.star.ApplicationInstance.Stopping;
import static com.codeaffine.tiny.star.ApplicationInstanceImpl.*;
import static com.codeaffine.tiny.star.Texts.INFO_SHUTDOWN_CONFIRMATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import java.util.stream.Stream;

class ApplicationInstanceImplTest {

    private static final String IDENTIFIER = "identifier";

    private LifecycleConsumerListener lifecycleConsumingListener;
    private ParameterlessListener parameterlessListener;
    private ApplicationInstanceImpl applicationInstance;
    private Runnable terminator;
    private Runnable starter;
    private Logger logger;

    interface LifecycleConsumerListener {
        @Starting
        void starting(ApplicationInstance applicationInstance);
        @Started
        void started(ApplicationInstance applicationInstance);
        @Stopping
        void stopping(ApplicationInstance applicationInstance);
        @Stopped
        void stopped(ApplicationInstance applicationInstance);
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

    static class StartingStateCaptor {

        State captured;

        @Starting
        void captureState(ApplicationInstance applicationInstance) {
            captured = applicationInstance.getState();
        }
    }

    static class StoppingStateCaptor {

        State captured;

        @Stopping
        void captureState(ApplicationInstance applicationInstance) {
            captured = applicationInstance.getState();
        }
    }

    @BeforeEach
    void setUp() {
        terminator = mock(Runnable.class);
        starter = mock(Runnable.class);
        logger = mock(Logger.class);
        applicationInstance = new ApplicationInstanceImpl(IDENTIFIER, starter, terminator, logger);
        lifecycleConsumingListener = mock(LifecycleConsumerListener.class);
        parameterlessListener = mock(ParameterlessListener.class);
        applicationInstance.registerLifecycleListener(lifecycleConsumingListener);
        applicationInstance.registerLifecycleListener(parameterlessListener);
    }

    @Test
    void getIdentifier() {
        String actual = applicationInstance.getIdentifier();

        assertThat(actual).isEqualTo(IDENTIFIER);
    }

    @Test
    void start() {
        StartingStateCaptor startingStateCaptor = new StartingStateCaptor();
        applicationInstance.registerLifecycleListener(startingStateCaptor);

        State beforeState = applicationInstance.getState();
        applicationInstance.start();
        State afterState = applicationInstance.getState();

        assertThat(beforeState).isSameAs(HALTED);
        assertThat(startingStateCaptor.captured).isSameAs(STARTING);
        assertThat(afterState).isSameAs(RUNNING);
        verifyStartProcedure();
    }

    @Test
    void startIfRunning() {
        applicationInstance.start();
        reset(starter, lifecycleConsumingListener, parameterlessListener);

        State beforeState = applicationInstance.getState();
        applicationInstance.start();
        State afterState = applicationInstance.getState();

        verify(lifecycleConsumingListener, never()).starting(applicationInstance);
        verify(parameterlessListener, never()).starting();
        verify(starter, never()).run();
        verify(lifecycleConsumingListener, never()).started(applicationInstance);
        verify(parameterlessListener, never()).started();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_HALTED);
        assertThat(afterState)
            .isSameAs(beforeState)
            .isSameAs(RUNNING);
    }

    @Test
    void startOnStarting() {
        startOn().starting(applicationInstance);

        applicationInstance.start();

        verifyStartProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startOnStarted() {
        startOn().started(applicationInstance);

        applicationInstance.start();

        verifyStartProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startOnStopping() {
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        startOn().stopping(applicationInstance);

        applicationInstance.stop();

        verifyStopProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startOnStopped() {
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        startOn().stopped(applicationInstance);

        applicationInstance.stop();

        verifyStopProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_HALTED);
    }

    @Test
    void startWithStartingListenerThrowingException() {
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).starting();

        Throwable actual = catchThrowable(() -> applicationInstance.start());

        verify(lifecycleConsumingListener).starting(applicationInstance);
        verify(starter, never()).run();
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void startWithStartedListenerThrowingException() {
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).started();

        Throwable actual = catchThrowable(() -> applicationInstance.start());

        InOrder order = inOrder(starter, terminator, lifecycleConsumingListener, parameterlessListener);
        order.verify(lifecycleConsumingListener).starting(applicationInstance);
        order.verify(parameterlessListener).starting();
        order.verify(starter).run();
        order.verify(lifecycleConsumingListener).started(applicationInstance);
        order.verify(lifecycleConsumingListener).stopping(applicationInstance);
        order.verify(parameterlessListener).stopping();
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationInstance);
        order.verify(parameterlessListener).stopped();
        assertThat(actual).isSameAs(expected);
        verify(logger).error(Texts.ERROR_NOTIFYING_STARTED_LISTENER, expected);
        verify(logger).error(Texts.ENFORCING_APPLICATION_TERMINATION);
    }

    @Test
    void startWithStartedListenerAndStoppingThrowingException() {
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).started();
        throwGivenExceptionOnListenerMethod(expected).stopping();

        Throwable actual = catchThrowable(() -> applicationInstance.start());

        InOrder order = inOrder(starter, terminator, lifecycleConsumingListener, parameterlessListener, logger);
        order.verify(lifecycleConsumingListener).starting(applicationInstance);
        order.verify(parameterlessListener).starting();
        order.verify(starter).run();
        order.verify(lifecycleConsumingListener).started(applicationInstance);
        order.verify(lifecycleConsumingListener).stopping(applicationInstance);
        order.verify(logger).error(Texts.ERROR_NOTIFYING_STOPPING_LISTENER, expected);
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationInstance);
        order.verify(parameterlessListener).stopped();
        assertThat(actual).isSameAs(expected);
        verify(logger).error(Texts.ERROR_NOTIFYING_STARTED_LISTENER, expected);
        verify(logger).error(Texts.ENFORCING_APPLICATION_TERMINATION);
    }

    @Test
    void stop() {
        StoppingStateCaptor stoppingStateCaptor = new StoppingStateCaptor();
        applicationInstance.registerLifecycleListener(stoppingStateCaptor);
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);

        State beforeState = applicationInstance.getState();
        applicationInstance.stop();
        State afterState = applicationInstance.getState();

        assertThat(beforeState).isSameAs(RUNNING);
        assertThat(stoppingStateCaptor.captured).isSameAs(STOPPING);
        assertThat(afterState).isSameAs(HALTED);
        verifyStopProcedure();
        verify(logger).info(eq(INFO_SHUTDOWN_CONFIRMATION), eq(applicationInstance.getIdentifier()), anyLong());
    }

    @Test
    void stopIfHalted() {
        State beforeState = applicationInstance.getState();
        applicationInstance.stop();
        State afterState = applicationInstance.getState();

        verify(lifecycleConsumingListener, never()).stopping(applicationInstance);
        verify(parameterlessListener, never()).stopping();
        verify(terminator, never()).run();
        verify(lifecycleConsumingListener, never()).stopped(applicationInstance);
        verify(parameterlessListener, never()).stopped();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_RUNNING);
        assertThat(beforeState)
            .isSameAs(afterState)
            .isSameAs(HALTED);
    }

    @Test
    void stopOnStopping() {
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        stopOn().stopping(applicationInstance);

        applicationInstance.stop();

        verifyStopProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopOnStopped() {
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        stopOn().stopped(applicationInstance);

        applicationInstance.stop();

        verifyStopProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopOnStarting() {
        stopOn().starting(applicationInstance);

        applicationInstance.start();

        verifyStartProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopOnStarted() {
        stopOn().started(applicationInstance);

        applicationInstance.start();

        verifyStartProcedure();
        verify(logger).debug(Texts.DEBUG_APPLICATION_NOT_RUNNING);
    }

    @Test
    void stopWithStoppingListenerThrowingException() {
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).stopping();

        Throwable actual = catchThrowable(() -> applicationInstance.stop());

        InOrder order = inOrder(terminator, lifecycleConsumingListener, parameterlessListener, logger);
        order.verify(lifecycleConsumingListener).stopping(applicationInstance);
        order.verify(logger).error(Texts.ERROR_NOTIFYING_STOPPING_LISTENER, expected);
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationInstance);
        order.verify(parameterlessListener).stopped();
        order.verifyNoMoreInteractions();
        assertThat(actual)
            .isInstanceOf(LifecycleException.class)
            .hasMessage(Texts.ERROR_TERMINATING_APPLICATION);
    }

    @Test
    void stopWithStoppedListenerThrowingException() {
        applicationInstance.start();
        reset(terminator, lifecycleConsumingListener, parameterlessListener);
        RuntimeException expected = new RuntimeException("bad");
        throwGivenExceptionOnListenerMethod(expected).stopped();

        Throwable actual = catchThrowable(() -> applicationInstance.stop());

        InOrder order = inOrder(terminator, lifecycleConsumingListener, parameterlessListener, logger);
        order.verify(lifecycleConsumingListener).stopping(applicationInstance);
        order.verify(parameterlessListener).stopping();
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationInstance);
        order.verify(logger).error(Texts.ERROR_NOTIFYING_STOPPED_LISTENER, expected);
        order.verifyNoMoreInteractions();
        assertThat(actual)
            .isInstanceOf(LifecycleException.class)
            .hasMessage(Texts.ERROR_TERMINATING_APPLICATION);
    }

    @ParameterizedTest
    @MethodSource("provideLifecycleListenersWithIllegalSignature")
    void registerLifecycleListenerWithIllegalSignature(Object listenerWithIllegalMethodSignature) {
        Throwable actual = catchThrowable(() -> applicationInstance.registerLifecycleListener(listenerWithIllegalMethodSignature));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(listenerWithIllegalMethodSignature.getClass().getName())
            .hasMessageContaining("starting")
            .hasMessageContaining(ApplicationInstanceImpl.class.getName());
    }

    @Test
    void deregisterLifecycleListener() {
        applicationInstance.deregisterLifecycleListener(lifecycleConsumingListener);

        applicationInstance.start();

        InOrder order = inOrder(lifecycleConsumingListener, parameterlessListener);
        order.verify(parameterlessListener).starting();
        order.verify(parameterlessListener).started();
        order.verifyNoMoreInteractions();
    }

    @Test
    void constructWithNullAsNameArgument() {
        assertThatThrownBy(() -> new ApplicationInstanceImpl(null, starter, terminator, logger))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsStarterArgument() {
        assertThatThrownBy(() -> new ApplicationInstanceImpl("name", null, terminator, logger))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsTerminatorArgument() {
        assertThatThrownBy(() -> new ApplicationInstanceImpl("name", starter, null, logger))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsLoggerArgument() {
        assertThatThrownBy(() -> new ApplicationInstanceImpl("name", starter, terminator, null))
            .isInstanceOf(NullPointerException.class);
    }

    private void verifyStartProcedure() {
        InOrder order = inOrder(starter, lifecycleConsumingListener, parameterlessListener);
        order.verify(lifecycleConsumingListener).starting(applicationInstance);
        order.verify(parameterlessListener).starting();
        order.verify(starter).run();
        order.verify(lifecycleConsumingListener).started(applicationInstance);
        order.verify(parameterlessListener).started();
        order.verifyNoMoreInteractions();
    }

    private void verifyStopProcedure() {
        InOrder order = inOrder(terminator, lifecycleConsumingListener, parameterlessListener);
        order.verify(lifecycleConsumingListener).stopping(applicationInstance);
        order.verify(parameterlessListener).stopping();
        order.verify(terminator).run();
        order.verify(lifecycleConsumingListener).stopped(applicationInstance);
        order.verify(parameterlessListener).stopped();
        order.verifyNoMoreInteractions();
    }

    static Stream<Object> provideLifecycleListenersWithIllegalSignature() {
        return Stream.of(
            new Object() {
                @Starting
                void starting(String parameter) {
                }
            },
            new Object() {
                @Starting
                void starting(ApplicationInstanceImpl applicationInstanceImpl, String parameter) {
                }
            }
        );
    }

    private ParameterlessListener throwGivenExceptionOnListenerMethod(Exception exception) {
        return doThrow(exception).when(parameterlessListener);
    }

    private LifecycleConsumerListener startOn() {
        return doAnswer((Answer<Void>) this::triggerStart)
            .when(lifecycleConsumingListener);
    }

    private Void triggerStart(InvocationOnMock invocation) {
        applicationInstance.start();
        return null;
    }

    private LifecycleConsumerListener stopOn() {
        return doAnswer((Answer<Void>) this::triggerStop)
            .when(lifecycleConsumingListener);
    }

    private Void triggerStop(InvocationOnMock invocation) {
        applicationInstance.stop();
        return null;
    }
}
