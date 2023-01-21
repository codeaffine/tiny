package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static com.codeaffine.tiny.shared.Threads.sleepFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricTest {

    private static final long SIMULATED_OPERATION_DURATION_IN_MILLIS = 50L;

    static class SupplierOperation implements Supplier<Long> {

        @Override
        public Long get() {
            simulateOperationDuration();
            return SIMULATED_OPERATION_DURATION_IN_MILLIS;
        }
    }

    static class RunnableOperation implements Runnable {

        @Override
        public void run() {
            simulateOperationDuration();
        }
    }

    @Test
    void measureDurationOfSupplierOperation() {
        SupplierOperation operation = new SupplierOperation();
        AtomicLong reportedValue = new AtomicLong();
        AtomicLong reportedDuration = new AtomicLong();

        Long actual = Metric.measureDuration(operation)
            .report((value, duration) -> {
                reportedValue.set(value);
                reportedDuration.set(duration);
            });

        assertThat(actual)
            .isEqualTo(SIMULATED_OPERATION_DURATION_IN_MILLIS)
            .isEqualTo(reportedValue.get());
        assertThat(reportedDuration.get()).isGreaterThanOrEqualTo(SIMULATED_OPERATION_DURATION_IN_MILLIS);
    }

    @Test
    void measureDurationRunnableOperation() {
        RunnableOperation operation = new RunnableOperation();
        AtomicLong reportedDuration = new AtomicLong();

        Metric.measureDuration(operation)
            .report(reportedDuration::set);

        assertThat(reportedDuration.get()).isGreaterThanOrEqualTo(SIMULATED_OPERATION_DURATION_IN_MILLIS);
    }

    @Test
    void measureWithNullAsSupplierOperationArgument() {
        assertThatThrownBy(() -> Metric.measureDuration((Supplier<?>) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void measureWithNullAsRunnableOperationArgument() {
        assertThatThrownBy(() -> Metric.measureDuration((Runnable) null))
            .isInstanceOf(NullPointerException.class);
    }

    private static void simulateOperationDuration() {
        sleepFor(SIMULATED_OPERATION_DURATION_IN_MILLIS);
    }
}
