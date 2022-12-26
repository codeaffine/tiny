package com.codeaffine.tiny.star.extrinsic;

import com.codeaffine.tiny.star.LoggingFrameworkControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DelegatingLoggingFrameworkControlTest {

    private static final String UNAVAILABLE_SLF_4_J_SERVICE_PROVIDER_CLASSNAME = "unAvailableSlf4jServiceProviderClassName";
    private static final String APPLICATION_NAME = "applicationName";

    private LoggingFrameworkControl delegate;
    private LoggingFrameworkControl fallback;

    @BeforeEach
    void setUp() {
        delegate = stubLoggingFrameworkControlDelegate();
        fallback = stubLoggingFrameworkControlDelegate();
    }

    @Test
    void delegateMethodCalls() {
        Map<String, LoggingFrameworkControl> loggingFrameworksMap = Map.of(
            simulateLoadableSlf4jServiceProviderClassname1(), delegate,
            UNAVAILABLE_SLF_4_J_SERVICE_PROVIDER_CLASSNAME, delegate
        );
        DelegatingLoggingFrameworkControl control = new DelegatingLoggingFrameworkControl(fakeApplicationClassLoader(), loggingFrameworksMap, fallback);

        control.configure(fakeApplicationClassLoader(), APPLICATION_NAME);
        control.halt();
        boolean actual = control.isUsingWorkingDirectory();

        assertThat(actual).isTrue();
        verify(delegate).configure(fakeApplicationClassLoader(), APPLICATION_NAME);
        verify(delegate).halt();
        verify(delegate).isUsingWorkingDirectory();
        verify(fallback, never()).configure(fakeApplicationClassLoader(), APPLICATION_NAME);
        verify(fallback, never()).halt();
        verify(fallback, never()).isUsingWorkingDirectory();
    }

    @Test
    void delegateMethodCallsIfNoSupportedLoggingFrameworkIsUsed() {
        LoggingFrameworkControl delegate = stubLoggingFrameworkControlDelegate();
        LoggingFrameworkControl fallback = stubLoggingFrameworkControlDelegate();
        Map<String, LoggingFrameworkControl> loggingFrameworksMap = Map.of(
            UNAVAILABLE_SLF_4_J_SERVICE_PROVIDER_CLASSNAME, delegate
        );
        DelegatingLoggingFrameworkControl control = new DelegatingLoggingFrameworkControl(fakeApplicationClassLoader(), loggingFrameworksMap, fallback);

        control.configure(fakeApplicationClassLoader(), APPLICATION_NAME);
        control.halt();
        boolean actual = control.isUsingWorkingDirectory();

        assertThat(actual).isTrue();
        verify(delegate, never()).configure(fakeApplicationClassLoader(), APPLICATION_NAME);
        verify(delegate, never()).halt();
        verify(delegate, never()).isUsingWorkingDirectory();
        verify(fallback).configure(fakeApplicationClassLoader(), APPLICATION_NAME);
        verify(fallback).halt();
        verify(fallback).isUsingWorkingDirectory();
    }

    @Test
    void constructIfMultipleSlf4JLoggingFrameworkAdaptersAreOnTheClasspath() {
        Map<String, LoggingFrameworkControl> loggingFrameworksMap = Map.of(
            simulateLoadableSlf4jServiceProviderClassname1(), delegate,
            simulateLoadableSlf4jServiceProviderClassname2(), delegate
        );

        Throwable actual = catchThrowable(() -> new DelegatingLoggingFrameworkControl(fakeApplicationClassLoader(), loggingFrameworksMap, fallback));

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(simulateLoadableSlf4jServiceProviderClassname1())
            .hasMessageContaining(simulateLoadableSlf4jServiceProviderClassname1());
    }

    @Test
    @SuppressWarnings("unchecked")
    void constructWithNullAsApplicationClassLoaderArgument() {
        assertThatThrownBy(() -> new DelegatingLoggingFrameworkControl(null, EMPTY_MAP, delegate))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationSupportedLoggingFrameworksArgument() {
        ClassLoader applicationClassLoader = fakeApplicationClassLoader();

        assertThatThrownBy(() -> new DelegatingLoggingFrameworkControl(applicationClassLoader, null, delegate))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void constructWithNullAsFallbackArgument() {
        ClassLoader applicationClassLoader = fakeApplicationClassLoader();

        assertThatThrownBy(() -> new DelegatingLoggingFrameworkControl(applicationClassLoader, EMPTY_MAP, null))
            .isInstanceOf(NullPointerException.class);
    }

    private static LoggingFrameworkControl stubLoggingFrameworkControlDelegate() {
        LoggingFrameworkControl result = mock(LoggingFrameworkControl.class);
        when(result.isUsingWorkingDirectory()).thenReturn(true);
        return result;
    }

    private static String simulateLoadableSlf4jServiceProviderClassname1() {
        return DelegatingLoggingFrameworkControlTest.class.getName();
    }

    private static String simulateLoadableSlf4jServiceProviderClassname2() {
        return DelegatingLoggingFrameworkControl.class.getName();
    }

    private static ClassLoader fakeApplicationClassLoader() {
        return DelegatingLoggingFrameworkControlTest.class.getClassLoader();
    }
}
