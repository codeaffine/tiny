package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static com.codeaffine.tiny.star.common.Reflections.extractExceptionToReport;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

class ObserverRegistryTest {

    static final int OBSERVER_NOTIFICATION_TIMEOUT = 40;


    private ObservedConsumerListener observedConsumingListener;
    private ParameterlessListener parameterlessListener;
    private Observed observed;

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface ObserverAnnotation {}

    interface ObservedConsumerListener {
        @ObserverAnnotation
        void eventFired(Observed observed);
    }

    interface ParameterlessListener {
        @ObserverAnnotation
        void eventFired();
    }

    public static class SlowListener {
        @ObserverAnnotation
        public void eventFired() {
            sleepFor( OBSERVER_NOTIFICATION_TIMEOUT * 2 );
        }
    }

    static class Observed {

        private final ObserverRegistry<Observed> observerRegistry;

        Observed() {
            observerRegistry = new ObserverRegistry<>(this, Observed.class, OBSERVER_NOTIFICATION_TIMEOUT, ObserverAnnotation.class);
        }

        void registerObserver(Object lifecycleListener) {
            observerRegistry.registerObserver(lifecycleListener);
        }

        void deregisterObserver(Object lifecycleListener) {
            observerRegistry.deregisterObserver(lifecycleListener);
        }

        void fireEvent() {
            observerRegistry.notifyObservers(ObserverAnnotation.class, exception -> {
                throw extractExceptionToReport(exception, RuntimeException::new);
            });
        }
    }

    @BeforeEach
    void setUp() {
        observed = new Observed();
        observedConsumingListener = mock(ObservedConsumerListener.class);
        parameterlessListener = mock(ParameterlessListener.class);
        observed.registerObserver(observedConsumingListener);
        observed.registerObserver(parameterlessListener);
    }

    @Test
    void notifyObservers() {
        observed.fireEvent();

        InOrder order = inOrder(observedConsumingListener, parameterlessListener);
        order.verify(observedConsumingListener).eventFired(observed);
        order.verify(parameterlessListener).eventFired();
        order.verifyNoMoreInteractions();
    }

    @Test
    void notifyObserversIfListenerThrowsException() {
        RuntimeException expected = new RuntimeException();
        doThrow(expected).when(observedConsumingListener).eventFired(observed);

        Throwable actual = catchThrowable(() -> observed.fireEvent());

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void notifyObserversIfListenerExecutionExceedsTimeout() {
        observed.registerObserver(new SlowListener());

        Throwable actual = catchThrowable(() -> observed.fireEvent());

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(TimeoutException.class);
    }

    @ParameterizedTest
    @MethodSource("provideObserversWithIllegalSignature")
    void registerObserverWithIllegalSignature(Object listenerWithIllegalMethodSignature) {
        Throwable actual = catchThrowable(() -> observed.registerObserver(listenerWithIllegalMethodSignature));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(listenerWithIllegalMethodSignature.getClass().getName())
            .hasMessageContaining("eventFired")
            .hasMessageContaining(Observed.class.getName());
    }

    @Test
    void deregisterObserver() {
        observed.deregisterObserver(observedConsumingListener);

        observed.fireEvent();

        InOrder order = inOrder(observedConsumingListener, parameterlessListener);
        order.verify(parameterlessListener).eventFired();
        order.verifyNoMoreInteractions();
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new ObserverRegistry<>(null, Observed.class, OBSERVER_NOTIFICATION_TIMEOUT, ObserverAnnotation.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsArgumentTypeArgument() {
        assertThatThrownBy(() -> new ObserverRegistry<>(observed, null, OBSERVER_NOTIFICATION_TIMEOUT, ObserverAnnotation.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsAnnotationTypeArgument() {
        assertThatThrownBy(() -> new ObserverRegistry<>(observed, Observed.class, OBSERVER_NOTIFICATION_TIMEOUT, (Class<? extends Annotation>[]) null))
            .isInstanceOf(NullPointerException.class);
    }

    static Stream<Object> provideObserversWithIllegalSignature() {
        return Stream.of(
            new Object() {
                @ObserverAnnotation
                void eventFired(String parameter) {
                }
            },
            new Object() {
                @ObserverAnnotation
                void eventFired(Observed observed, String parameter) {
                }
            }
        );
    }
}
