/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.test.test.fixtures.UseLoggerSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.log4j.Log4j2Configurator.CONFIGURATION_FILE_NAME_SUFFIX;
import static com.codeaffine.tiny.star.log4j.Log4j2Configurator.logger;
import static com.codeaffine.tiny.star.log4j.Log4j2ConfiguratorTest.FakeConfigurator.initializeFakeConfigurator;
import static com.codeaffine.tiny.star.log4j.Texts.*;
import static java.lang.Thread.currentThread;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@UseLoggerSpy(Log4j2Configurator.class)
class Log4j2ConfiguratorTest {

    private static final String GLOBAL = "application-expecting-log4j2-configuration-only-available-to-system-classloader";
    private static final String UNKNOWN_CONFIGURATOR_CLASS = "unknownConfiguratorClass";
    private static final String CONFIGURATOR_CLASS = FakeConfigurator.class.getName();
    private static final String APPLICATION_NAME = TestConfiguration.class.getName();
    private static final String RECONFIGURE_METHOD = "reconfigure";
    private static final String ERROR_MESSAGE = "bad";

    private TestConfiguration configuration;

    static class FakeConfigurator {

        static final AtomicReference<Exception> problemHolder = new AtomicReference<>();
        static final AtomicBoolean reconfigureCalled = new AtomicBoolean(false);

        static void initializeFakeConfigurator() {
            reconfigureCalled.set(false);
            problemHolder.set(null);
        }

        @SuppressWarnings("unused")
        public static void reconfigure(@SuppressWarnings("unused") URI uri) throws Exception {
            if(nonNull(problemHolder.get())) {
                throw problemHolder.get();
            }
            reconfigureCalled.set(true);
        }
    }

    static class TestConfiguration {}

    @BeforeEach
    void setUp() {
        initializeFakeConfigurator();
        configuration = new TestConfiguration();
    }

    @Test
    void run() {
        Log4j2Configurator configurator = new Log4j2Configurator(CONFIGURATOR_CLASS, RECONFIGURE_METHOD, r -> null);

        configurator.run(getAppLoader(), APPLICATION_NAME);

        assertThat(FakeConfigurator.reconfigureCalled).isTrue();
        InOrder order = inOrder(logger);
        order.verify(logger).debug(LOG_LOG4J2_DETECTED, expectedConfigFileName());
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER, expectedConfigFileName());
        order.verify(logger).debug(LOG_LOG4J2_RECONFIGURATION_SUCCESSFUL, expectedConfigFileName());
        order.verifyNoMoreInteractions();
    }

    @Test
    void runIfContextClassLoaderIsNotSet() {
        Log4j2Configurator configurator = new Log4j2Configurator(CONFIGURATOR_CLASS, RECONFIGURE_METHOD, r -> null);
        currentThread().setContextClassLoader(null);

        configurator.run(getAppLoader(), APPLICATION_NAME);

        assertThat(FakeConfigurator.reconfigureCalled).isTrue();
        InOrder order = inOrder(logger);
        order.verify(logger).debug(LOG_LOG4J2_DETECTED, expectedConfigFileName());
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER, expectedConfigFileName());
        order.verify(logger).debug(LOG_LOG4J2_RECONFIGURATION_SUCCESSFUL, expectedConfigFileName());
        order.verifyNoMoreInteractions();
    }

    @Test
    void runIfContextClassLoaderCannotFindResource() {
        Log4j2Configurator configurator = new Log4j2Configurator(CONFIGURATOR_CLASS, RECONFIGURE_METHOD, r -> null);
        currentThread().setContextClassLoader(mock(ClassLoader.class));

        configurator.run(getAppLoader(), APPLICATION_NAME);

        assertThat(FakeConfigurator.reconfigureCalled).isTrue();
        InOrder order = inOrder(logger);
        order.verify(logger).debug(LOG_LOG4J2_DETECTED, expectedConfigFileName());
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER, expectedConfigFileName());
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER, expectedConfigFileName());
        order.verify(logger).debug(LOG_LOG4J2_RECONFIGURATION_SUCCESSFUL, expectedConfigFileName());
        order.verifyNoMoreInteractions();
    }

    @Test
    void runIfApplicationClassLoaderCannotFindResource()  {
        URL resource = getClass().getClassLoader().getResource(expectedConfigFileName());
        Log4j2Configurator configurator = new Log4j2Configurator(CONFIGURATOR_CLASS, RECONFIGURE_METHOD, any -> resource);

        configurator.run(getAppLoader(), GLOBAL);

        assertThat(FakeConfigurator.reconfigureCalled).isTrue();
        InOrder order = inOrder(logger);
        order.verify(logger).debug(LOG_LOG4J2_DETECTED, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_SYSTEM_CLASS_LOADER, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_LOG4J2_RECONFIGURATION_SUCCESSFUL, expectedConfigFileName(GLOBAL));
        order.verifyNoMoreInteractions();
    }

    @Test
    void runIfNoClassLoaderCanFindResource()  {
        Log4j2Configurator configurator = new Log4j2Configurator(CONFIGURATOR_CLASS, RECONFIGURE_METHOD, any -> null);

        configurator.run(getAppLoader(), GLOBAL);

        assertThat(FakeConfigurator.reconfigureCalled).isFalse();
        InOrder order = inOrder(logger);
        order.verify(logger).debug(LOG_LOG4J2_DETECTED, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_SYSTEM_CLASS_LOADER, expectedConfigFileName(GLOBAL));
        order.verify(logger).debug(LOG_LOG4J2_CONFIGURATION_NOT_FOUND, expectedConfigFileName(GLOBAL));
        order.verifyNoMoreInteractions();
    }

    @Test
    void runIfReconfigurationCallThrowException() {
        Log4j2Configurator configurator = new Log4j2Configurator(CONFIGURATOR_CLASS, RECONFIGURE_METHOD, r -> null);
        RuntimeException expected = new RuntimeException(ERROR_MESSAGE);
        FakeConfigurator.problemHolder.set(expected);

        Exception actual = catchException(() -> configurator.run(getAppLoader(), APPLICATION_NAME));

        assertThat(FakeConfigurator.reconfigureCalled).isFalse();
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void runIfLog4J2CannotBeDetected() {
        Log4j2Configurator configurator = new Log4j2Configurator(UNKNOWN_CONFIGURATOR_CLASS, RECONFIGURE_METHOD, r -> null);

        configurator.run(getAppLoader(), GLOBAL);

        assertThat(FakeConfigurator.reconfigureCalled).isFalse();
        verify(logger, never()).debug(anyString(), eq(expectedConfigFileName(GLOBAL)));
    }

    @Test
    void runWithNullAsConfigurationArgument() {
        Log4j2Configurator configurator = new Log4j2Configurator();

        assertThatThrownBy(() -> configurator.run(null, GLOBAL))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void runWithNullAsApplicationNameArgument() {
        ClassLoader appLoader = getAppLoader();
        Log4j2Configurator configurator = new Log4j2Configurator();

        assertThatThrownBy(() -> configurator.run(appLoader, null))
            .isInstanceOf(NullPointerException.class);
    }

    private static String expectedConfigFileName() {
        return expectedConfigFileName(APPLICATION_NAME);
    }

    private static String expectedConfigFileName(String applicationName) {
        return applicationName + CONFIGURATION_FILE_NAME_SUFFIX;
    }

    private ClassLoader getAppLoader() {
        return configuration.getClass().getClassLoader();
    }
}
