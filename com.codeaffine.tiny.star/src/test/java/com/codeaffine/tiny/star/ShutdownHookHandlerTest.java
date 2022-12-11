package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.State;
import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static com.codeaffine.tiny.star.ShutdownHookHandler.RuntimeSupplier;
import static com.codeaffine.tiny.star.ShutdownHookHandler.beforeProcessShutdown;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;

import com.codeaffine.tiny.star.SystemPrintStreamCaptor.SystemErrCaptor;

class ShutdownHookHandlerTest {

    private ShutdownHookHandler shutdownHookHandler;
    private Runtime runtime;

    @BeforeEach
    void setUp() {
        runtime = mock(Runtime.class);
        shutdownHookHandler = new ShutdownHookHandler(stubRuntimeSupplier(runtime));
    }
        
    @Test
    void register() throws InterruptedException {
        Runnable shutdownOperation = mock(Runnable.class);

        shutdownHookHandler.register(shutdownOperation);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        InOrder order = inOrder(runtime, shutdownOperation);
        order.verify(runtime).addShutdownHook(shutdownHookHandler.getShutdownHookThread());
        order.verify(shutdownOperation).run();
        order.verifyNoMoreInteractions();
    }

    @Test
    void registerMultipleShutdownOperations() throws InterruptedException {
        Runnable shutdownOperation1 = mock(Runnable.class);
        Runnable shutdownOperation2 = mock(Runnable.class);

        shutdownHookHandler.register(shutdownOperation1);
        shutdownHookHandler.register(shutdownOperation2);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        verify(shutdownOperation1).run();
        verify(shutdownOperation2).run();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void registerErrorProneOperation(SystemErrCaptor systemErrCaptor) throws InterruptedException {
        Runnable shutdownOperation1 = mock(Runnable.class);
        Runnable shutdownOperation2 = mock(Runnable.class);
        RuntimeException expected = stubOperationWithException(shutdownOperation2);

        shutdownHookHandler.register(shutdownOperation1);
        shutdownHookHandler.register(shutdownOperation2);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        verify(shutdownOperation1).run();
        verify(shutdownOperation2).run();
        assertThat(systemErrCaptor.getLog()).contains(expected.getMessage());
    }

    @Test
    void registerIfShutdownIsRunning() throws InterruptedException {
        Runnable shutdownOperation1 = mock(Runnable.class);
        Runnable shutdownOperation2 = mock(Runnable.class);
        Runnable shutdownOperation3 = spyRunnable(() -> shutdownHookHandler.register(shutdownOperation2));

        shutdownHookHandler.register(shutdownOperation1);
        shutdownHookHandler.register(shutdownOperation3);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        verify(shutdownOperation1).run();
        verify(shutdownOperation3).run();
        verify(shutdownOperation2, never()).run();
    }

    @Test
    void deregisterIfShutdownIsRunning() throws InterruptedException {
        Runnable shutdownOperation1 = mock(Runnable.class);
        Runnable shutdownOperation2 = mock(Runnable.class);
        Runnable shutdownOperation3 = spyRunnable(() -> shutdownHookHandler.deregister(shutdownOperation2));

        shutdownHookHandler.register(shutdownOperation1);
        shutdownHookHandler.register(shutdownOperation2);
        shutdownHookHandler.register(shutdownOperation3);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        verify(shutdownOperation1).run();
        verify(shutdownOperation2).run();
        verify(shutdownOperation3).run();
    }

    @Test
    void deregister() throws InterruptedException {
        Runnable shutdownOperation = mock(Runnable.class);

        shutdownHookHandler.register(shutdownOperation);
        shutdownHookHandler.deregister(shutdownOperation);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        InOrder order = inOrder(runtime, shutdownOperation);
        order.verify(runtime).addShutdownHook(shutdownHookHandler.getShutdownHookThread());
        order.verify(runtime).removeShutdownHook(shutdownHookHandler.getShutdownHookThread());
        order.verifyNoMoreInteractions();
    }

    @Test
    void deregisterIfShutdownHookIsRunning() throws InterruptedException {
        Runnable shutdownOperation = newSelfDeregisteringOperation(shutdownHookHandler);

        shutdownHookHandler.register(shutdownOperation);
        shutdownHookHandler.getShutdownHookThread().start();
        shutdownHookHandler.getShutdownHookThread().join();

        verify(runtime).addShutdownHook(shutdownHookHandler.getShutdownHookThread());
        verify(runtime, never()).removeShutdownHook(shutdownHookHandler.getShutdownHookThread());
    }

    @Test
    void registerWithNullAsShutdownOperationArgument() {
        ShutdownHookHandler shutdownHookHandler = new ShutdownHookHandler();

        assertThatThrownBy(() -> shutdownHookHandler.register(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deregisterWithNullAsShutdownOperationArgument() {
        ShutdownHookHandler shutdownHookHandler = new ShutdownHookHandler();

        assertThatThrownBy(() -> shutdownHookHandler.deregister(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getRuntime() {
        Runtime expected = Runtime.getRuntime();

        Runtime actual = new RuntimeSupplier().getRuntime();

        assertThat(actual).isSameAs(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "STARTING",
        "STOPPING",
        "HALTED"
    })
    void beforeProcessShutdownIfProcessStateIsNotRunning(State state) {
        Terminator terminator = mock(Terminator.class);
        ApplicationProcess applicationProcess = stubApplicationProcessGetState(state);

        beforeProcessShutdown(terminator, applicationProcess);

        InOrder order = inOrder(terminator, applicationProcess);
        order.verify(terminator).setShutdownHookExecution(true);
        order.verify(applicationProcess).getState();
        order.verify(terminator).deleteWorkingDirectory();
        order.verifyNoMoreInteractions();
    }

    @Test
    void beforeProcessShutdownIfProcwssIsRunning() {
        Terminator terminator = mock(Terminator.class);
        ApplicationProcess applicationProcess = stubApplicationProcessGetState(RUNNING);

        beforeProcessShutdown(terminator, applicationProcess);

        InOrder order = inOrder(terminator, applicationProcess);
        order.verify(terminator).setShutdownHookExecution(true);
        order.verify(applicationProcess).getState();
        order.verify(applicationProcess).stop();
        order.verifyNoMoreInteractions();
    }

    @Test
    void beforeProcessShutdownWithNullAsTerminatorArgument() {
        assertThatThrownBy(() -> beforeProcessShutdown(null, mock(ApplicationProcess.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void beforeProcessShutdownWithNullAsProcessArgument() {
        assertThatThrownBy(() -> beforeProcessShutdown(mock(Terminator.class), null))
            .isInstanceOf(NullPointerException.class);
    }

    private static ApplicationProcess stubApplicationProcessGetState(State state) {
        ApplicationProcess result = mock(ApplicationProcess.class);
        when(result.getState()).thenReturn(state);
        return result;
    }

    private static RuntimeSupplier stubRuntimeSupplier(Runtime runtime) {
        RuntimeSupplier result = mock(RuntimeSupplier.class);
        when(result.getRuntime()).thenReturn(runtime);
        return result;
    }

    private static RuntimeException stubOperationWithException(Runnable shutdownOperation) {
        RuntimeException result = new RuntimeException("bad");
        doThrow(result).when(shutdownOperation).run();
        return result;
    }

    private static Runnable newSelfDeregisteringOperation(ShutdownHookHandler shutdownHookHandler) {
        return new Runnable() {
            @Override
            public void run() {
                Runnable runnable = () -> shutdownHookHandler.deregister(this);
                runnable.run();
            }
        };
    }

    private static Runnable spyRunnable(Runnable runnable) {
        return spy(new Runnable() { // in general, it is not allowed to spy a lambda expression
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
