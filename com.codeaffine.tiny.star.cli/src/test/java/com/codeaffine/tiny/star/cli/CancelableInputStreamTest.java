/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.shared.Synchronizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.shared.Threads.sleepFor;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CancelableInputStreamTest {

    private static final String CONTENT = "content";
    private static final String CANCEL_SIGNAL = "cancelSignal";
    private static final long SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS = 25;
    private static final ByteArrayInputStream CANCEL_SIGNAL_STREAM = new ByteArrayInputStream(CANCEL_SIGNAL.getBytes(UTF_8));

    private ExecutorService executor;
    private LockableInputStream delegate;
    private CancelableInputStream inputStream;

    static class LockableInputStream extends ByteArrayInputStream {

        private final Synchronizer synchronizer;

        private boolean simulateAwaitingOfBytes;

        LockableInputStream(byte[] buf) {
            super(buf);
            synchronizer = new Synchronizer();
        }

        @Override
        public int available() {
            return synchronizer.execute(this::doAvailable);
        }

        void simulateAwaitingOfBytes() {
            synchronizer.execute(() -> simulateAwaitingOfBytes = true);
        }

        void endAwaitingOfBytesSimulation() {
            synchronizer.execute(() -> simulateAwaitingOfBytes = false);
        }

        private int doAvailable() {
            if(simulateAwaitingOfBytes) {
                return 0;
            }
            return super.available();
        }
    }

    @BeforeEach
    void setUp() {
        executor = newSingleThreadExecutor();
        delegate = new LockableInputStream(CONTENT.getBytes(UTF_8));
        inputStream = new CancelableInputStream(delegate, CANCEL_SIGNAL_STREAM, SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        delegate.endAwaitingOfBytesSimulation();
        executor.shutdown();
        boolean terminated = executor.awaitTermination(100, MILLISECONDS);
        assertThat(terminated).isTrue();
    }

    @Test
    void read() throws IOException {
        byte[] buffer = newEmptyByteArrayWithLengthOfContent();

        int actual = inputStream.read(buffer, 0, CONTENT.length());

        assertThat(actual).isEqualTo(CONTENT.length());
        assertThat(buffer).isEqualTo(CONTENT.getBytes(UTF_8));
    }

    @Test
    void readOnNextBytesAwaitingInput() {
        byte[] buffer = newEmptyByteArrayWithLengthOfContent();
        AtomicBoolean readTriggered = new AtomicBoolean();
        AtomicBoolean readDone = new AtomicBoolean();
        delegate.simulateAwaitingOfBytes();

        Future<Integer> awaiting
            = executor.submit(() -> readOnNextBytesAwaitingInput(() -> inputStream.read(buffer, 0, CONTENT.length()), readTriggered, readDone));
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);

        assertThat(awaiting.isDone()).isFalse();
        assertThat(buffer).isEqualTo(newEmptyByteArrayWithLengthOfContent());
        assertThat(readTriggered).isTrue();
        assertThat(readDone).isFalse();
    }

    @Test
    void cancelReadOnNextBytesAwaitingInputStream() {
        byte[] buffer = newEmptyByteArrayWithLengthOfContent();
        AtomicBoolean readTriggered = new AtomicBoolean();
        AtomicBoolean readDone = new AtomicBoolean();
        delegate.simulateAwaitingOfBytes();

        executor.submit(() -> readOnNextBytesAwaitingInput(() -> inputStream.read(buffer, 0, CONTENT.length()), readTriggered, readDone));
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        inputStream.cancel();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);

        assertThat(buffer).isEqualTo(CANCEL_SIGNAL.substring(0, CONTENT.length()).getBytes(UTF_8));
        assertThat(readTriggered).isTrue();
        assertThat(readDone).isTrue();
    }

    @Test
    void readOnNextBytesAwaitingInputStreamWithInterruptOnThreadSuspension() throws ExecutionException, InterruptedException {
        long suspendTimeLongEnoughToReachInterruptCall = SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 4;
        inputStream = new CancelableInputStream(delegate, CANCEL_SIGNAL_STREAM, suspendTimeLongEnoughToReachInterruptCall);
        byte[] buffer = newEmptyByteArrayWithLengthOfContent();
        AtomicReference<Thread> threadCaptor = new AtomicReference<>();
        delegate.simulateAwaitingOfBytes();

        Future<Integer> actual = executor.submit(() -> readOnNextBytesAwaitingInput(() -> inputStream.read(buffer, 0, CONTENT.length()), threadCaptor));
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        threadCaptor.get().interrupt();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);

        assertThat(buffer).isEqualTo(newEmptyByteArrayWithLengthOfContent());
        assertThat(actual.get()).isEqualTo(CancelableInputStream.NO_MORE_DATA);
    }

    @Test
    void close() throws IOException {
        InputStream delegateInputStream = mock(InputStream.class);
        CancelableInputStream cancelableInputStream = new CancelableInputStream(delegateInputStream, CANCEL_SIGNAL);

        cancelableInputStream.close();

        verify(delegateInputStream, never()).close();
    }

    @Test
    void constructWithNullAsDelegateArgument() {
        InputStream inputStream = null;

        assertThatThrownBy(() -> new CancelableInputStream(inputStream, CANCEL_SIGNAL))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsCancelSignalArgument() {
        assertThatThrownBy(() -> new CancelableInputStream(delegate, null))
            .isInstanceOf(NullPointerException.class);
    }

    private static byte[] newEmptyByteArrayWithLengthOfContent() {
        return new byte[CONTENT.length()];
    }

    private int readOnNextBytesAwaitingInput(Callable<Integer> readOperation, AtomicBoolean readTriggeredCaptor, AtomicBoolean readDoneCaptor) {
        readTriggeredCaptor.set(true);
        int result = readOnNextBytesAwaitingInput(readOperation);
        readDoneCaptor.set(true);
        return result;
    }

    private static int readOnNextBytesAwaitingInput(Callable<Integer> integerCallable, AtomicReference<Thread> threadCaptor) {
        threadCaptor.set(currentThread());
        return readOnNextBytesAwaitingInput(integerCallable);
    }

    private static int readOnNextBytesAwaitingInput(Callable<Integer> readOperation) {
        try {
            return readOperation.call();
        } catch (Exception cause) {
            throw new IllegalStateException(cause);
        }
    }
}
