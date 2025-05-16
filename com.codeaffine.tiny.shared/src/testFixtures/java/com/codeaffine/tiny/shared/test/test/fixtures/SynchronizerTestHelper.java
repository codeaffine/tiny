/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared.test.test.fixtures;

import com.codeaffine.tiny.shared.Synchronizer;
import lombok.NoArgsConstructor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;

import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * <p>SynchronizerTestHelper is a utility class that provides a fake instance of the
 * {@link Synchronizer} class for testing purposes. It allows you to simulate the
 * behavior of the {@link Synchronizer} class without actually acquiring locks or
 * executing operations.</p>
 *
 * <p> Testing concurrent code is difficult because it is hard to reproduce the
 * timing of the threads. However, it is possible to check that particular operations
 * are executed in a thread-safe manner. To do so this class provides a fake
 * instance by the {@link #fakeSynchronizer()} that is a
 * <a href="https://site.mockito.org/">mockito</a> mock object which delegates the
 * execution of {@link Runnable} and {@link Supplier} operations to the provided
 * implementations.</p>
 *
 * <p>Having a fake instance of the {@link Synchronizer} class allows you to
 * verify that the expected operations are actually executed in a
 * thread-safe manner by verifying that such operations are NOT executed, if
 * the fake synchronizer instance is reset (Mockito.reset(Object...)).</p>
 */
@NoArgsConstructor(access = PRIVATE)
public class SynchronizerTestHelper {

    /**
     * Creates a fake instance of the {@link Synchronizer} class.
     * <p> This method uses <a href="https://site.mockito.org/">mockito</a> to create
     * a mock object that simulates the behavior of the {@link Synchronizer} class.</p>
     *
     * @return a fake instance of the {@link Synchronizer} class
     */
    @SuppressWarnings("unchecked")
    public static Synchronizer fakeSynchronizer() {
        Synchronizer result = mock(Synchronizer.class);
        doAnswer(SynchronizerTestHelper::executeRunnable)
            .when(result)
            .execute(ArgumentMatchers.any(Runnable.class));
        doAnswer(SynchronizerTestHelper::executeSupplier)
            .when(result)
            .execute(ArgumentMatchers.any(Supplier.class));
        return result;
    }

    private static Void executeRunnable(InvocationOnMock invocation) {
        Runnable runnable = invocation.getArgument(0, Runnable.class);
        runnable.run();
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Object executeSupplier(InvocationOnMock invocation) {
        Supplier<Object> supplier = invocation.getArgument(0, Supplier.class);
        return supplier.get();
    }
}
