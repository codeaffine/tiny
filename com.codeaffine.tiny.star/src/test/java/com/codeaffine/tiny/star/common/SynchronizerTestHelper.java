package com.codeaffine.tiny.star.common;

import lombok.NoArgsConstructor;
import org.mockito.invocation.InvocationOnMock;

import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.Mockito.*;

@NoArgsConstructor(access = PRIVATE)
public class SynchronizerTestHelper {

    @SuppressWarnings("unchecked")
    public static Synchronizer fakeSynchronizer() {
        Synchronizer result = mock(Synchronizer.class);
        doAnswer(SynchronizerTestHelper::executeRunnable)
            .when(result)
            .execute(any(Runnable.class));
        doAnswer(SynchronizerTestHelper::executeSupplier)
            .when(result)
            .execute(any(Supplier.class));
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
