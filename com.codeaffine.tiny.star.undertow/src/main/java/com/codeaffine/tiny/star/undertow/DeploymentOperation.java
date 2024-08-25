/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.codeaffine.tiny.star.undertow.SessionTimeoutConfigurator.*;
import static io.undertow.servlet.Servlets.*;
import static jakarta.servlet.DispatcherType.REQUEST;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class DeploymentOperation {

    static final String DEPLOYMENT_NAME = "application.war";
    static final String CONTEXT_PATH = "/";

    @NonNull
    private final ServerConfiguration configuration;

    DeploymentManager deployRwtApplication(@NonNull ServletInfo servletInfo) {
        DeploymentManager result = defaultContainer()
            .addDeployment(configureDeployment(servletInfo));
        result.deploy();
        return result;
    }

    private DeploymentInfo configureDeployment(ServletInfo servletInfo) {
        DeploymentInfo deploymentInfo = deployment()
            .setClassLoader(configuration.getContextClassLoader())
            .setContextPath(CONTEXT_PATH)
            .addDeploymentCompleteListener(configuration.getContextListener())
            .setDeploymentName(DEPLOYMENT_NAME)
            .addServlets(servletInfo)
            .addServletContainerInitializer(newServletContainerInitializerInfo(configuration))
            .addFilters(createFilterInfos());
        configuration.getFilterDefinitions()
            .forEach(filterDefinition -> addFilterMappings(filterDefinition, servletInfo, deploymentInfo));
        return deploymentInfo
            .setResourceManager(new FileResourceManager(configuration.getWorkingDirectory(), 1));
    }

    private List<FilterInfo> createFilterInfos() {
        return configuration.getFilterDefinitions()
            .stream()
            .map(definition -> filter(definition.getFilterName(), definition.getFilter().getClass(), () -> new FilterInstanceHandle(definition)))
            .toList();
    }

    private static void addFilterMappings(FilterDefinition filterDefinition, ServletInfo servletInfo, DeploymentInfo deploymentInfo) {
        List<String> urlPatterns = filterDefinition.getUrlPatterns();
        if(!urlPatterns.isEmpty()) {
            urlPatterns.forEach(urlPattern -> deploymentInfo.addFilterUrlMapping(filterDefinition.getFilterName(), urlPattern, REQUEST));
        } else {
            deploymentInfo.addFilterServletNameMapping(filterDefinition.getFilterName(), servletInfo.getName(), REQUEST);
        }
    }
}
