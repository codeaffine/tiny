/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerTestHelper.*;
import static com.codeaffine.tiny.star.undertow.DeploymentOperation.*;
import static jakarta.servlet.DispatcherType.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentOperationTest {

    @TempDir
    private File workingDirectory;

    @Test
    void deployRwtApplication() {
        DeploymentOperation deploymentOperation = new DeploymentOperation(stubServerConfiguration(workingDirectory, ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2));
        ServletInfoFactory servletInfoFactory = new ServletInfoFactory(MULTI_ENTRYPOINT_CONFIGURATION);
        ServletInfo servletInfo = servletInfoFactory.createRwtServletInfo();

        DeploymentManager actual = deploymentOperation.deployRwtApplication(servletInfo);

        DeploymentInfo deploymentInfo = actual.getDeployment().getDeploymentInfo();
        FileResourceManager resourceManager = (FileResourceManager) deploymentInfo.getResourceManager();
        assertThat(deploymentInfo.getContextPath()).isEqualTo(CONTEXT_PATH);
        assertThat(deploymentInfo.getDeploymentName()).isEqualTo(DEPLOYMENT_NAME);
        assertThat(deploymentInfo.getClassLoader()).isSameAs(MULTI_ENTRYPOINT_CONFIGURATION.getClass().getClassLoader());
        assertThat(deploymentInfo.getServlets())
            .hasSize(1)
            .allSatisfy((key, value) -> assertThat(key).isEqualTo(servletInfo.getName()))
            .allSatisfy((key, value) -> assertThat(value.getName()).isEqualTo(servletInfo.getName()));
        assertThat(deploymentInfo.getDeploymentCompleteListeners())
            .hasSize(1)
            .allSatisfy(listener -> assertThat(listener).isInstanceOf(TinyStarServletContextListener.class));
        assertThat(deploymentInfo.getServletContainerInitializers())
            .hasSize(1)
            .allSatisfy(initializer -> assertThat(initializer.getServletContainerInitializerClass()).isSameAs(SessionTimeoutConfigurator.class));
        assertThat(resourceManager.getBase()).isEqualTo(workingDirectory);
    }

    @Test
    void deployRwtApplicationWithFilterDefinitions() {
        DeploymentOperation deploymentOperation = new DeploymentOperation(stubServerConfiguration(
            workingDirectory,
            List.of(FILTER_DEFINITION_1, FILTER_DEFINITION_2, FILTER_DEFINITION_3),
            List.of(ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2)
        ));
        ServletInfoFactory servletInfoFactory = new ServletInfoFactory(MULTI_ENTRYPOINT_CONFIGURATION);
        ServletInfo servletInfo = servletInfoFactory.createRwtServletInfo();

        DeploymentManager actual = deploymentOperation.deployRwtApplication(servletInfo);

        DeploymentInfo deploymentInfo = actual.getDeployment().getDeploymentInfo();
        assertThat(deploymentInfo.getFilters())
            .hasSize(3)
            .containsKey(FILTER_NAME_1)
            .containsKey(FILTER_NAME_2)
            .containsKey(FILTER_NAME_3);
        assertThat(deploymentInfo.getFilterMappings())
            .hasSize(4)
            .allSatisfy(mapping -> assertThat(mapping.getDispatcher()).isEqualTo(REQUEST))
            .anySatisfy(mapping -> {
                assertThat(mapping.getFilterName()).isEqualTo(FILTER_NAME_1);
                assertThat(mapping.getMapping()).isEqualTo(servletInfo.getName());
            })
            .anySatisfy(mapping -> {
                assertThat(mapping.getFilterName()).isEqualTo(FILTER_NAME_2);
                assertThat(mapping.getMapping()).isEqualTo(ENTRYPOINT_PATH_1);
            })
            .anySatisfy(mapping -> {
                assertThat(mapping.getFilterName()).isEqualTo(FILTER_NAME_3);
                assertThat(mapping.getMapping()).isEqualTo(ENTRYPOINT_PATH_1);
            })
            .anySatisfy(mapping -> {
                assertThat(mapping.getFilterName()).isEqualTo(FILTER_NAME_3);
                assertThat(mapping.getMapping()).isEqualTo(ENTRYPOINT_PATH_2);
            });
    }

    @Test
    void deployRwtApplicationWithNullAsArgumentNameArgument() {
        DeploymentOperation deploymentOperation = new DeploymentOperation(stubServerConfiguration(workingDirectory, ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2));

        assertThatThrownBy(() -> deploymentOperation.deployRwtApplication(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new DeploymentOperation(null))
            .isInstanceOf(NullPointerException.class);
    }
}
