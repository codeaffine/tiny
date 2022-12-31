package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.io.File;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class DeploymentOperation {

    static final String CONTEXT_PATH = "/";
    static final String DEPLOYMENT_NAME = "application.war";

    @NonNull
    private final ApplicationConfiguration applicationConfiguration;
    @NonNull
    private final File workingDirectory;

    DeploymentManager deployRwtApplication(@NonNull ServletInfo servletInfo) {
        DeploymentManager result = defaultContainer()
            .addDeployment(configureDeployment(servletInfo));
        result.deploy();
        return result;
    }

    private DeploymentInfo configureDeployment(ServletInfo servletInfo) {
        return deployment()
            .setClassLoader(applicationConfiguration.getClass().getClassLoader())
            .setContextPath(CONTEXT_PATH)
            .addDeploymentCompleteListener(new TinyStarServletContextListener(applicationConfiguration))
            .setDeploymentName(DEPLOYMENT_NAME)
            .addServlets(servletInfo)
            .setResourceManager(new FileResourceManager(workingDirectory, 1));
    }
}
