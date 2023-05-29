/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.*;

class SynchronizerTest {

    private Synchronizer synchronizer;
    private Lock lock;

    @BeforeEach
    void setUp() {
        lock = mock(Lock.class);
        synchronizer = new Synchronizer(lock);
    }

    @Test
    void executeWithRunnable() {
        Runnable runnable = mock(Runnable.class);

        synchronizer.execute(runnable);

        verifyRunnableLockHandlingOnExecute(runnable);
    }

    @Test
    void executeWithExceptionThrowingRunnable() {
        RuntimeException expected = new RuntimeException();
        Runnable runnable = mock(Runnable.class);
        doThrow(expected).when(runnable).run();

        Exception actual = catchException(() -> synchronizer.execute(runnable));

        assertThat(actual).isSameAs(expected);
        verifyRunnableLockHandlingOnExecute(runnable);
    }

    @Test
    void executeWithExceptionThrowingSupplier() {
        RuntimeException expected = new RuntimeException();
        Supplier<Object> supplier = stubSupplier(new Object());
        doThrow(expected).when(supplier).get();

        Exception actual = catchException(() -> synchronizer.execute(supplier));

        assertThat(actual).isSameAs(expected);
        verifyRunnableLockHandlingOnExecute(supplier);
    }

    @Test
    void executeWithSupplier() {
        Object expected = new Object();
        Supplier<Object> supplier = stubSupplier(expected);

        Object actual = synchronizer.execute(supplier);

        assertThat(actual).isSameAs(expected);
        verifyRunnableLockHandlingOnExecute(supplier);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<Object> stubSupplier(Object expected) {
        Supplier<Object> result = mock(Supplier.class);
        when(result.get()).thenReturn(expected);
        return result;
    }

    private void verifyRunnableLockHandlingOnExecute(Runnable runnable) {
        InOrder order = inOrder(lock, runnable);
        order.verify(lock).lock();
        order.verify(runnable).run();
        order.verify(lock).unlock();
        order.verifyNoMoreInteractions();
    }

    private void verifyRunnableLockHandlingOnExecute(Supplier<?> supplier) {
        InOrder order = inOrder(lock, supplier);
        order.verify(lock).lock();
        order.verify(supplier).get();
        order.verify(lock).unlock();
        order.verifyNoMoreInteractions();
    }
}
