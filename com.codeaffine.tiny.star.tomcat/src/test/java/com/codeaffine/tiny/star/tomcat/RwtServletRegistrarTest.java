package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class RwtServletRegistrarTest {

    private static final String ENTRYPOINT_PATH_1 = "/foo";
    private static final String ENTRYPOINT_PATH_2 = "/bar";
    private static final ApplicationConfiguration APPLICATION_CONFIGURATION = application -> {
        application.addEntryPoint(ENTRYPOINT_PATH_1, TestEntryPoint.class, null);
        application.addEntryPoint(ENTRYPOINT_PATH_2, TestEntryPoint.class, null);
    };

    static class TestEntryPoint extends AbstractEntryPoint {
        @Override
        protected void createContents(Composite parent) {
        }
    }

    @Test
    void addRwtServlet() throws ServletException {
        Tomcat tomcat = mock(Tomcat.class);
        Context context = stubContext();
        TinyStarServletContextListener contextListener = mock(TinyStarServletContextListener.class);
        RwtServletRegistrar registrar = new RwtServletRegistrar(tomcat, APPLICATION_CONFIGURATION, contextListener);

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
        assertThatThrownBy(() -> new RwtServletRegistrar(null, APPLICATION_CONFIGURATION))
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
