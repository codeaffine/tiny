package com.codeaffine.tiny.star.common;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.codeaffine.tiny.star.common.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.common.Texts.*;
import static org.assertj.core.api.Assertions.*;

class ReflectionsTest {

    @Test
    void extractExceptionToReportWithArbitraryCheckedExceptionAsExceptionArgument() {
        Exception expected = new Exception();

        RuntimeException actual = extractExceptionToReport(expected, IllegalStateException::new);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(expected);
    }

    @Test
    void extractExceptionToReportIfExceptionArgumentIsInstanceOfReturnTypeOfRuntimeExceptionFactory() {
        IllegalStateException expected = new IllegalStateException();

        RuntimeException actual = extractExceptionToReport(expected, IllegalStateException::new);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void extractExceptionToReportIfIfExceptionArgumentIsNotInstanceOfReturnTypeOfRuntimeExceptionFactory() {
        RuntimeException cause = new RuntimeException();

        RuntimeException actual = extractExceptionToReport(cause, IllegalStateException::new);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(cause);
    }

    @Test
    void extractExceptionToReportIfIfExceptionArgumentIsNotInstanceOfReturnTypeOfRuntimeExceptionFactoryButForwardRuntimeExceptionIsUsedAsModeArgument() {
        RuntimeException expected = new RuntimeException();

        RuntimeException actual = extractExceptionToReport(expected, IllegalStateException::new, Reflections.Mode.FORWARD_RUNTIME_EXCEPTIONS);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void extractExceptionToReportIfExceptionArgumentIsInstanceOfInvocationExceptionHavingCauseInstanceOfReturnTypeOfRuntimeExceptionFactory() {
        IllegalStateException expected = new IllegalStateException();
        InvocationTargetException invocationTargetException = new InvocationTargetException(expected);

        RuntimeException actual = extractExceptionToReport(invocationTargetException, IllegalStateException::new);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void extractExceptionToReportIfExceptionArgumentIsInstanceOfInvocationExceptionHavingCauseNotInstanceOfReturnTypeOfRuntimeExceptionFactory() {
        RuntimeException expected = new RuntimeException();
        InvocationTargetException invocationTargetException = new InvocationTargetException(expected);

        RuntimeException actual = extractExceptionToReport(invocationTargetException, IllegalStateException::new);

        assertThat(actual)
            .isInstanceOf(RuntimeException.class)
            .hasCause(expected);
    }

    @Test
    void extractExceptionToReportWithInvocationTargetExceptionAsExceptionArgumentHavingCheckedCause() {
        Exception expected = new Exception();
        InvocationTargetException invocationTargetException = new InvocationTargetException(expected);

        RuntimeException actual = extractExceptionToReport(invocationTargetException, IllegalStateException::new);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(expected);
    }

    @Test
    void extractExceptionToReportWithExecutionExceptionAsExceptionArgument() {
        RuntimeException expected = new RuntimeException();
        ExecutionException executionException = new ExecutionException(expected);

        RuntimeException actual = extractExceptionToReport(executionException, RuntimeException::new);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void extractExceptionToReportWithRuntimeExceptionFactoryArgumentReturningNullWrapper() {
        Exception expected = new Exception();

        @SuppressWarnings("ThrowableNotThrown") Throwable actual = catchThrowable(() -> extractExceptionToReport(expected, throwable -> null));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_NULL);
    }

    @Test
    void extractExceptionToReportWithRuntimeExceptionFactoryArgumentReturningWrapperWithoutCause() {
        Exception expected = new Exception();

        @SuppressWarnings("ThrowableNotThrown") Throwable actual = catchThrowable(() -> extractExceptionToReport(expected, throwable -> new RuntimeException()));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITHOUT_CAUSE);
    }

    @Test
    void extractExceptionToReportWithRuntimeExceptionFactoryArgumentReturningWrapperWithWrongCause() {
        Exception expected = new Exception();

        @SuppressWarnings("ThrowableNotThrown") Throwable actual = catchThrowable(() -> extractExceptionToReport(expected, throwable -> new RuntimeException(new IllegalStateException())));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITH_WRONG_CAUSE);
    }

    @Test
    void extractExceptionToReportWithNullAsExceptionArgument() {
        Function<Throwable, RuntimeException> runtimeExceptionFactory = RuntimeException::new;

        //noinspection ThrowableNotThrown
        assertThatThrownBy(() -> extractExceptionToReport(null, runtimeExceptionFactory, Reflections.Mode.FORWARD_RUNTIME_EXCEPTIONS))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void extractExceptionToReportWithNullAsRuntimeExceptionFactoryArgument() {
        Exception exception = new Exception();

        //noinspection ThrowableNotThrown
        assertThatThrownBy(() -> extractExceptionToReport(exception, null, Reflections.Mode.FORWARD_RUNTIME_EXCEPTIONS))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void extractExceptionToReportWithNullAsModeArgument() {
        Exception exception = new Exception();

        //noinspection ThrowableNotThrown
        assertThatThrownBy(() -> extractExceptionToReport(exception, RuntimeException::new, null))
            .isInstanceOf(NullPointerException.class);
    }
}
