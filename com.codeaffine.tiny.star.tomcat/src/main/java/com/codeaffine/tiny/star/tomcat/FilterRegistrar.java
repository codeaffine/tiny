/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.util.List;

import static com.codeaffine.tiny.star.tomcat.RwtServletRegistrar.SERVLET_NAME;
import static jakarta.servlet.DispatcherType.REQUEST;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class FilterRegistrar {

    @NonNull
    private final ServerConfiguration configuration;

    void addFilters(@NonNull Context context) {
        configuration.getFilterDefinitions()
            .forEach(filterDefinition -> addFilter(context, filterDefinition));
    }

    private static void addFilter(Context context, FilterDefinition filterDefinition) {
        context.addFilterDef(newFilterDef(filterDefinition));
        addUrlMappings(context, filterDefinition);
    }

    private static FilterDef newFilterDef(FilterDefinition filterDefinition) {
        FilterDef result = new FilterDef();
        result.setFilter(filterDefinition.getFilter());
        result.setFilterName(filterDefinition.getFilterName());
        return result;
    }

    private static void addUrlMappings(Context context, FilterDefinition filterDefinition) {
        List<String> urlPatterns = filterDefinition.getUrlPatterns();
        if(!urlPatterns.isEmpty()) {
            context.addFilterMap(newUrlPatternFilterMap(filterDefinition, urlPatterns));
        } else {
            context.addFilterMap(newServletFilterMap(filterDefinition));
        }
    }

    private static FilterMap newServletFilterMap(FilterDefinition filterDefinition) {
        FilterMap result = new FilterMap();
        result.setFilterName(filterDefinition.getFilterName());
        result.addServletName(SERVLET_NAME);
        result.setDispatcher(REQUEST.name());
        return result;
    }

    private static FilterMap newUrlPatternFilterMap(FilterDefinition filterDefinition, List<String> urlPatterns) {
        FilterMap result = new FilterMap();
        result.setFilterName(filterDefinition.getFilterName());
        urlPatterns.forEach(result::addURLPattern);
        result.setDispatcher(REQUEST.name());
        return result;
    }
}
