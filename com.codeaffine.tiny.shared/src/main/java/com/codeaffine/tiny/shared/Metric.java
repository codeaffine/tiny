/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NonNull;

import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;

/**
 * helper class for measuring the execution time of operations for reporting
 */
public class Metric {

    private Metric() {
        // prevent instance creation
    }
    
    /**
     * Duration result reporting class for operations with return value.
     *
     * @param <T> the operations return value type.
     */
    public static class SupplierDurationReporter<T> {
        
        private final T valueComputedByOperation;
        private final long durationOfOperation;

        SupplierDurationReporter(T valueComputedByOperation, long durationOfOperation) {
            this.valueComputedByOperation = valueComputedByOperation;
            this.durationOfOperation = durationOfOperation;
        }

        /**
         * callback handler for reporting of the operation's execution time.
         *
         * @param consumer callback that accepts the return value computed by the measured operation and the operation's execution time in milliseconds.
         * @return the value computed by the operation.
         */
        public T report(ObjLongConsumer<T> consumer) {
            consumer.accept(valueComputedByOperation, durationOfOperation);
            return valueComputedByOperation;
        }
    }

    /**
     * Duration result reporting class for operations without return value.
     *
     */
    public static class RunnableDurationReporter {
        
        private final long durationOfOperation;
        
        RunnableDurationReporter(long durationOfOperation) {
            this.durationOfOperation = durationOfOperation;
        }

        /**
         * callback handler for reporting of the operation's execution time.
         *
         * @param consumer callback that accepts the operation's execution time in milliseconds.
         */
        public void report(LongConsumer consumer) {
            consumer.accept(durationOfOperation);
        }
    }

    /**
     * executes the given operation and measures the operation's execution time.
     *
     * @param <T> the operations return value type.
     * @param operation the operation to execute.
     * @return a reporting callback handler that allows to accept execution time and operation result. 
     */
    public static <T> SupplierDurationReporter<T> measureDuration(@NonNull Supplier<? extends T> operation) {
        long start = currentTimeMillis();
        T valueComputedByOperation = operation.get();
        long durationOfOperation = currentTimeMillis() - start;
        return new SupplierDurationReporter<>(valueComputedByOperation, durationOfOperation);
    }

    /**
     * executes the given operation and measures the operation's execution time.
     *
     * @param operation the operation to execute.
     * @return a reporting callback handler that allows to accept the operations' execution time.
     */
    public static RunnableDurationReporter measureDuration(@NonNull Runnable operation) {
        long start = currentTimeMillis();
        operation.run();
        long durationOfOperation = currentTimeMillis() - start;
        return new RunnableDurationReporter(durationOfOperation);
    }
}
