/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.qos.logback.classic.ClassicConstants.CONFIG_FILE_PROPERTY;
import static ch.qos.logback.classic.spi.Configurator.ExecutionStatus;
import static ch.qos.logback.classic.spi.Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogbackConfiguratorTest {

    private Configurator lockbackDefaultConfigurator;
    private LoggerContext context;

    @BeforeEach
    void setUp() {
        LogbackConfigurator.singleton = null;
        context = mock(LoggerContext.class);
        lockbackDefaultConfigurator = mock(Configurator.class);
    }

    @AfterEach
    void tearDown() {
        LogbackConfigurator.singleton = null;
        System.getProperties().remove(CONFIG_FILE_PROPERTY);
    }

    @Test
    void construct() {
        LogbackConfigurator singletonBefore = LogbackConfigurator.singleton;
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator();
        LogbackConfigurator singletonAfter = LogbackConfigurator.singleton;

        assertThat(singletonBefore).isNull();
        assertThat(logbackConfigurator)
            .isInstanceOf(LogbackConfigurator.class)
            .isInstanceOf(Configurator.class)
            .isInstanceOf(LoggingFrameworkControl.class);
        assertThat(logbackConfigurator.logbackDefaultConfigurator).isNotNull();
        assertThat(singletonAfter).isSameAs(logbackConfigurator);
    }

    @Test
    void createLoggingFrameworkControl() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator();

        LoggingFrameworkControl actual = logbackConfigurator.create();

        assertThat(actual).isSameAs(LogbackConfigurator.singleton);
    }

    @Test
    void configureLogbackConfigurator() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator();

        ExecutionStatus actual = logbackConfigurator.configure(context);

        assertThat(actual).isEqualTo(DO_NOT_INVOKE_NEXT_IF_ANY);
        assertThat(LogbackConfigurator.singleton.context).isSameAs(context);
    }

    @Test
    void configureLogbackConfiguratorWithNullAsContextArgument() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator();

        assertThatThrownBy(() -> logbackConfigurator.configure(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void configureLoggingFrameworkControl() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        logbackConfigurator.configure(context);
        String application = "application";

        logbackConfigurator.configure(getClass().getClassLoader(), application);

        assertThat(System.getProperty(CONFIG_FILE_PROPERTY)).isEqualTo(application + LogbackConfigurator.CONFIGURATION_FILE_NAME_SUFFIX);
        assertThat(LogbackConfigurator.singleton.configured).isTrue();
        verify(lockbackDefaultConfigurator).configure(LogbackConfigurator.singleton.context);
    }

    @Test
    void configureLoggingFrameworkControlMoreThanOnceUsingDifferentApplicationName() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        logbackConfigurator.configure(context);
        String firstApplicationName = "firstApplication";
        String secondApplicationName = "secondApplication";

        logbackConfigurator.configure(getClass().getClassLoader(), firstApplicationName);
        logbackConfigurator.configure(getClass().getClassLoader(), secondApplicationName);

        assertThat(System.getProperty(CONFIG_FILE_PROPERTY)).isEqualTo(firstApplicationName + LogbackConfigurator.CONFIGURATION_FILE_NAME_SUFFIX);
        assertThat(LogbackConfigurator.singleton.configured).isTrue();
        verify(lockbackDefaultConfigurator).configure(LogbackConfigurator.singleton.context);
    }

    @Test
    void configureLoggingFrameworkControlWithNullAsApplicationNameArgument() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        ClassLoader classLoader = getClass().getClassLoader();

        assertThatThrownBy(() -> logbackConfigurator.configure(classLoader, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void setAndGetOfContext() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator();

        logbackConfigurator.setContext(context);
        Context actual = logbackConfigurator.getContext();

        assertThat(actual)
            .isSameAs(context)
            .isSameAs(LogbackConfigurator.singleton.getContext());
    }

    @Test
    void addStatus() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        Status expected = mock(Status.class);

        logbackConfigurator.addStatus(expected);

        verify(lockbackDefaultConfigurator).addStatus(expected);
    }

    @Test
    void addInfo() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        String message = "message";

        logbackConfigurator.addInfo(message);

        verify(lockbackDefaultConfigurator).addInfo(message);
    }

    @Test
    void addInfoWithException() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        String message = "message";
        Exception exception = mock(Exception.class);

        logbackConfigurator.addInfo(message, exception);

        verify(lockbackDefaultConfigurator).addInfo(message, exception);
    }

    @Test
    void addWarn() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        String message = "message";

        logbackConfigurator.addWarn(message);

        verify(lockbackDefaultConfigurator).addWarn(message);
    }

    @Test
    void addWarnWithException() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        String message = "message";
        Exception exception = mock(Exception.class);

        logbackConfigurator.addWarn(message, exception);

        verify(lockbackDefaultConfigurator).addWarn(message, exception);
    }

    @Test
    void addError() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        String message = "message";

        logbackConfigurator.addError(message);

        verify(lockbackDefaultConfigurator).addError(message);
    }

    @Test
    void addErrorWithException() {
        LogbackConfigurator logbackConfigurator = new LogbackConfigurator(lockbackDefaultConfigurator);
        String message = "message";
        Exception exception = mock(Exception.class);

        logbackConfigurator.addError(message, exception);

        verify(lockbackDefaultConfigurator).addError(message, exception);
    }
}
