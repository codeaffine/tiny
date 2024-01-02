/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.List;

import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.*;
import static com.codeaffine.tiny.star.tomcat.RwtServletRegistrar.SERVLET_NAME;
import static jakarta.servlet.DispatcherType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class FilterRegistrarTest {

    private static final int EXPECTED_AMOUNT_OF_FILTER_DEFINITIONS = 3;

    @Test
    void addFilters() {
        FilterRegistrar filterRegistrar = new FilterRegistrar(stubServerConfiguration(
            new File("workingDirectory"),
            List.of(FILTER_DEFINITION_1, FILTER_DEFINITION_2, FILTER_DEFINITION_3),
            List.of(ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2)
        ));
        Context context = mock(Context.class);

        filterRegistrar.addFilters(context);
        
        ArgumentCaptor<FilterDef> filterDefArgumentCaptor = forClass(FilterDef.class);
        verify(context, times(EXPECTED_AMOUNT_OF_FILTER_DEFINITIONS)).addFilterDef(filterDefArgumentCaptor.capture());
        assertThat(filterDefArgumentCaptor.getAllValues())
            .hasSize(EXPECTED_AMOUNT_OF_FILTER_DEFINITIONS)
            .anySatisfy(filterDef -> assertThat(filterDef.getFilterName()).isEqualTo(FILTER_NAME_1))
            .anySatisfy(filterDef -> assertThat(filterDef.getFilterName()).isEqualTo(FILTER_NAME_2))
            .anySatisfy(filterDef -> assertThat(filterDef.getFilterName()).isEqualTo(FILTER_NAME_3));
        ArgumentCaptor<FilterMap> filterMapArgumentCaptor = forClass(FilterMap.class);
        verify(context, times(EXPECTED_AMOUNT_OF_FILTER_DEFINITIONS)).addFilterMap(filterMapArgumentCaptor.capture());
        assertThat(filterMapArgumentCaptor.getAllValues())
            .hasSize(EXPECTED_AMOUNT_OF_FILTER_DEFINITIONS)
            .allSatisfy(filterMap -> assertThat(filterMap.getDispatcherNames()).containsExactly(REQUEST.name()))
            .anySatisfy(filterMap -> {
                assertThat(filterMap.getServletNames()).containsExactly(SERVLET_NAME);
                assertThat(filterMap.getFilterName()).isEqualTo(FILTER_NAME_1);
                assertThat(filterMap.getURLPatterns()).isEmpty();
            })
            .anySatisfy(filterMap -> {
                assertThat(filterMap.getFilterName()).isEqualTo(FILTER_NAME_2);
                assertThat(filterMap.getURLPatterns()).containsExactly(ENTRYPOINT_PATH_1);
            })
            .anySatisfy(filterMap -> {
                assertThat(filterMap.getFilterName()).isEqualTo(FILTER_NAME_3);
                assertThat(filterMap.getURLPatterns()).containsExactly(ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2);
            });
    }

    @Test
    void addFiltersWithNullAsContextArgument() {
        FilterRegistrar filterRegistrar = new FilterRegistrar(mock(ServerConfiguration.class));

        assertThatThrownBy(() -> filterRegistrar.addFilters(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new FilterRegistrar(null))
            .isInstanceOf(NullPointerException.class);
    }
}
