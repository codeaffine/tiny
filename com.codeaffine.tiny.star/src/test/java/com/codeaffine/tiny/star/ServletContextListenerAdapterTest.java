/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class ServletContextListenerAdapterTest {

    private ServletContextEvent servletContextEvent;
    private TinyStarServletContextListener tinyStarServletContextListener;
    private ServletContextListener applicationServletContextListener;
    private ServletContextListenerAdapter adapter;

    @BeforeEach
    void setUp() {
        servletContextEvent = mock(ServletContextEvent.class);
        tinyStarServletContextListener = mock(TinyStarServletContextListener.class);
        applicationServletContextListener = mock(ServletContextListener.class);
        adapter = new ServletContextListenerAdapter(tinyStarServletContextListener, List.of(applicationServletContextListener));
    }

    @Test
    void contextInitialized() {
        adapter.contextInitialized(servletContextEvent);

        InOrder order = inOrder(tinyStarServletContextListener, applicationServletContextListener);
        order.verify(tinyStarServletContextListener).contextInitialized(servletContextEvent);
        order.verify(applicationServletContextListener).contextInitialized(servletContextEvent);
        order.verifyNoMoreInteractions();
    }

    @Test
    void contextDestroyed() {
        adapter.contextDestroyed(servletContextEvent);

        InOrder order = inOrder(tinyStarServletContextListener, applicationServletContextListener);
        order.verify(applicationServletContextListener).contextDestroyed(servletContextEvent);
        order.verify(tinyStarServletContextListener).contextDestroyed(servletContextEvent);
        order.verifyNoMoreInteractions();
    }

    @Test
    void constructWithNullAsTinyStarServletContextListenerArgument() {
        List<ServletContextListener> applicationServletContextListeners = emptyList();

        assertThatThrownBy(() -> new ServletContextListenerAdapter(null, applicationServletContextListeners))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationServletContextListenersArgument() {
        assertThatThrownBy(() -> new ServletContextListenerAdapter(mock(TinyStarServletContextListener.class), null))
            .isInstanceOf(NullPointerException.class);
    }
}
