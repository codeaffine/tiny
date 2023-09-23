/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static com.codeaffine.tiny.star.tomcat.Texts.SERVER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ServerImplTest {

    private ServerImpl server;
    private ContextRegistrar contextRegistrar;
    private ConnectorRegistrar connectorRegistrar;
    private ResourcesServletRegistrar resourcesServletRegistrar;
    private RwtServletRegistrar rwtServletRegistrar;
    private TomcatLifeCycleControl lifeCycleControl;
    private FilterRegistrar filterRegistrar;

    @BeforeEach
    void setUp() {
        contextRegistrar = mock(ContextRegistrar.class);
        connectorRegistrar = mock(ConnectorRegistrar.class);
        resourcesServletRegistrar = mock(ResourcesServletRegistrar.class);
        rwtServletRegistrar = mock(RwtServletRegistrar.class);
        lifeCycleControl = mock(TomcatLifeCycleControl.class);
        filterRegistrar = mock(FilterRegistrar.class);
        server = new ServerImpl(
            contextRegistrar,
            connectorRegistrar,
            resourcesServletRegistrar,
            rwtServletRegistrar,
            filterRegistrar,
            lifeCycleControl
        );
    }

    @Test
    void start() {
        Context context = mock(Context.class);
        stubContextRegistrarAddContext(context);

        server.start();

        InOrder order = inOrder(contextRegistrar, connectorRegistrar, resourcesServletRegistrar, rwtServletRegistrar, filterRegistrar, lifeCycleControl);
        order.verify(contextRegistrar).addContext();
        order.verify(connectorRegistrar).addConnector();
        order.verify(resourcesServletRegistrar).addResourcesServlet(context);
        order.verify(rwtServletRegistrar).addRwtServlet(context);
        order.verify(filterRegistrar).addFilters(context);
        order.verify(lifeCycleControl).startTomcat();
        order.verifyNoMoreInteractions();
    }

    @Test
    void stop() {
        server.stop();

        verify(lifeCycleControl).stopTomcat();
    }

    @Test
    void getName() {
        String actual = server.getName();

        assertThat(actual).isEqualTo(SERVER_NAME);
    }

    private void stubContextRegistrarAddContext(Context context) {
        when(contextRegistrar.addContext()).thenReturn(context);
    }
}
