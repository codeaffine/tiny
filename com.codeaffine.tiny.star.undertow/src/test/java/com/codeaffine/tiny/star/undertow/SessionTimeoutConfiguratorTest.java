/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SessionTimeoutConfiguratorTest {

    private static final int SESSION_TIMEOUT_SETTING = 30;

    private SessionTimeoutConfigurator configurator;
    private ServerConfiguration configuration;
    private ServletContext context;

    @BeforeEach
    void setUp() {
        configuration = stubServerConfiguration();
        configurator = new SessionTimeoutConfigurator(configuration);
        context = mock(ServletContext.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void newServletContainerInitializerInfo() throws InstantiationException, ServletException {
        ServletContainerInitializerInfo info = SessionTimeoutConfigurator.newServletContainerInitializerInfo(configuration);
        ImmediateInstanceFactory<SessionTimeoutConfigurator> instanceFactory = (ImmediateInstanceFactory<SessionTimeoutConfigurator>) info.getInstanceFactory();
        InstanceHandle<SessionTimeoutConfigurator> actual = instanceFactory.createInstance();
        actual.getInstance().onStartup(null, context);

        verify(context).setSessionTimeout(SESSION_TIMEOUT_SETTING);
    }

    @Test
    void onStartup() throws ServletException {
        configurator.onStartup(null, context);

        verify(context).setSessionTimeout(SESSION_TIMEOUT_SETTING);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new SessionTimeoutConfigurator(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void onStartupWithNullAsContextArgument() {
        Set<Class<?>> classes = Collections.emptySet();

        assertThatThrownBy(() -> configurator.onStartup(classes, null))
            .isInstanceOf(NullPointerException.class);
    }

    private static ServerConfiguration stubServerConfiguration() {
        ServerConfiguration result = mock(ServerConfiguration.class);
        when(result.getSessionTimeout()).thenReturn(SESSION_TIMEOUT_SETTING);
        return result;
    }
}
