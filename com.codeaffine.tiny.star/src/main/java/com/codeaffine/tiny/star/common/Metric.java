package com.codeaffine.tiny.star.common;

import static java.lang.System.currentTimeMillis;

import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import lombok.NonNull;

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
    public static <T> SupplierDurationReporter<T> measureDuration(@NonNull Supplier<T> operation) {
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
