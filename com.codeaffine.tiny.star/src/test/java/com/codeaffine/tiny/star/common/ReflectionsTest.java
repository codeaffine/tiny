package com.codeaffine.tiny.star.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

class ReflectionsTest {

    @Test
    void extractExceptionToReportWithArbitraryCheckedException() {
        Exception expected = new Exception();

        RuntimeException actual = Reflections.extractExceptionToReport(expected, IllegalStateException::new);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(expected);
    }

    @Test
    void extractExceptionToReportWithArbitraryRuntimeException() {
        RuntimeException expected = new RuntimeException();

        RuntimeException actual = Reflections.extractExceptionToReport(expected, throwable -> null);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void extractExceptionToReportWithInvocationTargetExceptionHavingRuntimeCause() {
        RuntimeException expected = new RuntimeException();
        InvocationTargetException invocationTargetException = new InvocationTargetException(expected);

        RuntimeException actual = Reflections.extractExceptionToReport(invocationTargetException, throwable -> null);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void extractExceptionToReportWithInvocationTargetExceptionHavingCheckedCause() {
        Exception expected = new Exception();
        InvocationTargetException invocationTargetException = new InvocationTargetException(expected);

        RuntimeException actual = Reflections.extractExceptionToReport(invocationTargetException, IllegalStateException::new);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(expected);
    }
}
