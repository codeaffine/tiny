package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.codeaffine.tiny.star.ApplicationServerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class RwtServletRegistrarTest {

    @Test
    void addRwtServlet() throws ServletException {
        Tomcat tomcat = mock(Tomcat.class);
        Context context = stubContext();
        TinyStarServletContextListener contextListener = mock(TinyStarServletContextListener.class);
        RwtServletRegistrar registrar = new RwtServletRegistrar(tomcat, MULTI_ENTRYPOINT_CONFIGURATION, contextListener);

        registrar.addRwtServlet(context);
        simulateContainerStartup(context);

        ArgumentCaptor<RwtServletAdapter> servletCaptor = forClass(RwtServletAdapter.class);
        verify(tomcat).addServlet(eq(ContextRegistrar.CONTEXT_PATH), eq(RwtServletRegistrar.SERVLET_NAME), servletCaptor.capture());
        verify(context).addServletMappingDecoded(ENTRYPOINT_PATH_1 + RwtServletRegistrar.ALL_SUB_PATHS_PATTERN, RwtServletRegistrar.SERVLET_NAME);
        verify(context).addServletMappingDecoded(ENTRYPOINT_PATH_2 + RwtServletRegistrar.ALL_SUB_PATHS_PATTERN, RwtServletRegistrar.SERVLET_NAME);
        verify(contextListener).contextInitialized(any());
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

    private static void simulateContainerStartup(Context context) throws ServletException {
        ArgumentCaptor<ServletContainerInitializer> initializerCaptor = forClass(ServletContainerInitializer.class);
        verify(context).addServletContainerInitializer(initializerCaptor.capture(), isNull());
        ServletContainerInitializer containerInitializer = initializerCaptor.getValue();
        containerInitializer.onStartup(null, mock(jakarta.servlet.ServletContext.class));
    }
}
