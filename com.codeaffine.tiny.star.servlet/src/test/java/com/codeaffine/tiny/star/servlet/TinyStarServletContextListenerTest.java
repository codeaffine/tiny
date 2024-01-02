/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class TinyStarServletContextListenerTest {

    private static final String INTERNAL_APPLICATION_CONTEXT_INSTANCE_ATTRIBUTE_NAME
        = "org.eclipse.rap.rwt.internal.application.ApplicationContextImpl#instance";

    @TempDir
    private File tempDir;
    private TinyStarServletContextListener listener;

    @BeforeEach
    void setUp() {
        tempDir.deleteOnExit();
        listener = new TinyStarServletContextListener(application -> {});
    }

    @Test
    void contextInitialized() {
        ServletContext servletContext = stubServletContext();

        listener.contextInitialized(new ServletContextEvent(servletContext));

        ArgumentCaptor<ApplicationContext> appContextCaptor = forClass(ApplicationContext.class);
        verify(servletContext).setAttribute(eq(INTERNAL_APPLICATION_CONTEXT_INSTANCE_ATTRIBUTE_NAME), appContextCaptor.capture());
        assertThat(appContextCaptor.getValue()).isNotNull();
        assertThat(tempDir).isNotEmptyDirectory();
    }

    @Test
    void contextInitializedWithNullAsEventArgument() {
        assertThatThrownBy(() -> listener.contextInitialized(null))
            .isInstanceOf(NullPointerException.class);
    }
    @Test
    void contextDestroyed() {
        ServletContext servletContext = stubServletContext();
        listener.contextInitialized(new ServletContextEvent(servletContext));

        listener.contextDestroyed(new ServletContextEvent(servletContext));

        verify(servletContext).removeAttribute(INTERNAL_APPLICATION_CONTEXT_INSTANCE_ATTRIBUTE_NAME);
    }
    
    @Test
    void contextDestroyedWithNullAsEventArgument() {
        assertThatThrownBy(() -> listener.contextDestroyed(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new TinyStarServletContextListener(null))
            .isInstanceOf(NullPointerException.class);
    }
    

    private ServletContext stubServletContext() {
        ServletContext result = mock(ServletContext.class);
        when(result.getAttribute(ApplicationConfiguration.RESOURCE_ROOT_LOCATION)).thenReturn(tempDir.getAbsolutePath());
        return result;
    }
}
