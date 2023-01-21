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

    @BeforeEach
    void setUp() {
        contextRegistrar = mock(ContextRegistrar.class);
        connectorRegistrar = mock(ConnectorRegistrar.class);
        resourcesServletRegistrar = mock(ResourcesServletRegistrar.class);
        rwtServletRegistrar = mock(RwtServletRegistrar.class);
        lifeCycleControl = mock(TomcatLifeCycleControl.class);
        server = new ServerImpl(
            contextRegistrar,
            connectorRegistrar,
            resourcesServletRegistrar,
            rwtServletRegistrar,
            lifeCycleControl
        );
    }

    @Test
    void start() {
        Context context = mock(Context.class);
        stubContextRegistrarAddContext(context);

        server.start();

        InOrder order = inOrder(contextRegistrar, connectorRegistrar, resourcesServletRegistrar, rwtServletRegistrar, lifeCycleControl);
        order.verify(contextRegistrar).addContext();
        order.verify(connectorRegistrar).addConnector();
        order.verify(resourcesServletRegistrar).addResourcesServlet(context);
        order.verify(rwtServletRegistrar).addRwtServlet(context);
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
