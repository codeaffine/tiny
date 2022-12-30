package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ResourcesServletRegistrarTest {

    private ResourcesServletRegistrar registrar;

    @BeforeEach
    void setUp() {
        registrar = new ResourcesServletRegistrar();
    }

    @Test
    void addResourcesServlet() {
        Wrapper wrapper = mock(Wrapper.class);
        Context context = stubContext(wrapper);

        registrar.addResourcesServlet(context);

        verify(wrapper).setName(ResourcesServletRegistrar.SERVLET_NAME);
        verify(wrapper).setServletClass(ResourcesServletRegistrar.SERVLET_CLASS_NAME);
        verify(wrapper).addInitParameter(ResourcesServletRegistrar.INIT_PARAMETER_DEBUG, ResourcesServletRegistrar.INIT_VALUE_DEBUG);
        verify(wrapper).addInitParameter(ResourcesServletRegistrar.INIT_PARAMETER_LISTINGS, ResourcesServletRegistrar.INIT_VALUE_LISTINGS);
        verify(wrapper).setLoadOnStartup(ResourcesServletRegistrar.LOAD_ON_STARTUP);
        verify(context).addChild(wrapper);
        verify(context).addServletMappingDecoded(ResourcesServletRegistrar.PATTERN, ResourcesServletRegistrar.SERVLET_NAME);
    }

    @Test
    void addResourcesServletWithNullAsContextArgument() {
        assertThatThrownBy(() -> registrar.addResourcesServlet(null))
            .isInstanceOf(NullPointerException.class);
    }

    private static Context stubContext(Wrapper wrapper) {
        Context result = mock(Context.class);
        when(result.createWrapper()).thenReturn(wrapper);
        return result;
    }
}
