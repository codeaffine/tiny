/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class RwtServletRegistrarTest {

    @Test
    void addRwtServlet() throws ServletException {
        Context context = stubContext();
        Wrapper wrapper = mock(Wrapper.class);
        Tomcat tomcat = stubTomcat(wrapper);
        TinyStarServletContextListener contextListener = mock(TinyStarServletContextListener.class);
        RwtServletRegistrar registrar = new RwtServletRegistrar(tomcat, MULTI_ENTRYPOINT_CONFIGURATION, contextListener);

        registrar.addRwtServlet(context);
        simulateContainerStartup(context);

        ArgumentCaptor<RwtServletAdapter> servletCaptor = forClass(RwtServletAdapter.class);
        verify(tomcat).addServlet(eq(ContextRegistrar.CONTEXT_PATH), eq(RwtServletRegistrar.SERVLET_NAME), servletCaptor.capture());
        verify(context).addServletMappingDecoded(ENTRYPOINT_PATH_1 + RwtServletRegistrar.ALL_SUB_PATHS_PATTERN, RwtServletRegistrar.SERVLET_NAME);
        verify(context).addServletMappingDecoded(ENTRYPOINT_PATH_2 + RwtServletRegistrar.ALL_SUB_PATHS_PATTERN, RwtServletRegistrar.SERVLET_NAME);
        verify(contextListener).contextInitialized(any());
        verify(wrapper).setLoadOnStartup(RwtServletRegistrar.LOAD_ON_STARTUP);
        assertThat(servletCaptor.getValue()).isNotNull();
    }

    @Test
    void constructWithNullAsTomcatArgument() {
        assertThatThrownBy(() -> new RwtServletRegistrar(null, MULTI_ENTRYPOINT_CONFIGURATION))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationConfigurationArgument() {
        assertThatThrownBy(() -> new RwtServletRegistrar(mock(Tomcat.class), null))
            .isInstanceOf(NullPointerException.class);
    }

    private static Context stubContext() {
        Context result = mock(Context.class);
        when(result.getPath()).thenReturn(ContextRegistrar.CONTEXT_PATH);
        return result;
    }

    private static Tomcat stubTomcat(Wrapper wrapper) {
        Tomcat result = mock(Tomcat.class);
        when(result.addServlet(eq(ContextRegistrar.CONTEXT_PATH), eq(RwtServletRegistrar.SERVLET_NAME), any(Servlet.class)))
            .thenReturn(wrapper);
        return result;
    }

    private static void simulateContainerStartup(Context context) throws ServletException {
        ArgumentCaptor<ServletContainerInitializer> initializerCaptor = forClass(ServletContainerInitializer.class);
        verify(context).addServletContainerInitializer(initializerCaptor.capture(), isNull());
        ServletContainerInitializer containerInitializer = initializerCaptor.getValue();
        containerInitializer.onStartup(null, mock(jakarta.servlet.ServletContext.class));
    }
}
