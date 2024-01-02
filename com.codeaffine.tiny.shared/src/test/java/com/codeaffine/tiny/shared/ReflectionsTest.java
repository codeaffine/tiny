/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.shared.Texts.*;
import static lombok.AccessLevel.PACKAGE;
import static org.assertj.core.api.Assertions.*;

class ReflectionsTest {

    static class Operation implements Runnable {
        @Override
        public void run() {
        }
    }

    @RequiredArgsConstructor(access = PACKAGE)
    static class OperationWithConstructorArgument implements Runnable {

        final String argument;

        @Override
        public void run() {

        }
    }

    static class OperationWithConstructorThrowingException implements Runnable {

        private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException();

        OperationWithConstructorThrowingException() {
            throw RUNTIME_EXCEPTION;
        }

        @Override
        public void run() {

        }
    }

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

        @SuppressWarnings("ThrowableNotThrown")
        Exception actual = catchException(() -> extractExceptionToReport(expected, throwable -> null));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_NULL);
    }

    @Test
    void extractExceptionToReportWithRuntimeExceptionFactoryArgumentReturningWrapperWithoutCause() {
        Exception expected = new Exception();

        @SuppressWarnings("ThrowableNotThrown")
        Exception actual = catchException(() -> extractExceptionToReport(expected, throwable -> new RuntimeException()));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITHOUT_CAUSE);
    }

    @Test
    void extractExceptionToReportWithRuntimeExceptionFactoryArgumentReturningWrapperWithWrongCause() {
        Exception expected = new Exception();

        @SuppressWarnings("ThrowableNotThrown")
        Exception actual = catchException(() -> extractExceptionToReport(expected, throwable -> new RuntimeException(new IllegalStateException())));

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

    @Test
    void newInstance() {
        Runnable actual = Reflections.newInstance(Operation.class);

        assertThat(actual).isNotNull();
    }

    @Test
    void newInstanceWithTypeArgumentExpectingConstructorArgument() {
        Exception actual = catchException(() -> Reflections.newInstance(OperationWithConstructorArgument.class));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(NoSuchMethodException.class);
    }

    @Test
    void newInstanceWithTypeArgumentHavingConstructorThrowingException() {
        Exception actual = catchException(() -> Reflections.newInstance(OperationWithConstructorThrowingException.class));

        assertThat(actual)
            .isSameAs(OperationWithConstructorThrowingException.RUNTIME_EXCEPTION);
    }

    @Test
    void newInstanceWithNullAsTypeArgument() {
        assertThatThrownBy(() -> Reflections.newInstance(null))
            .isInstanceOf(NullPointerException.class);
    }
}
