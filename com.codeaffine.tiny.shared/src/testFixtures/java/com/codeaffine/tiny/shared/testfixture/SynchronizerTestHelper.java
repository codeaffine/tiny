/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared.testfixture;

import com.codeaffine.tiny.shared.Synchronizer;
import lombok.NoArgsConstructor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;

import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@NoArgsConstructor(access = PRIVATE)
public class SynchronizerTestHelper {

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
