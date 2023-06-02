/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
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
        return deployment()
            .setClassLoader(configuration.getContextClassLoader())
            .setContextPath(CONTEXT_PATH)
            .addDeploymentCompleteListener(configuration.getContextListener())
            .setDeploymentName(DEPLOYMENT_NAME)
            .addServlets(servletInfo)
            .setResourceManager(new FileResourceManager(configuration.getWorkingDirectory(), 1));
    }
}
