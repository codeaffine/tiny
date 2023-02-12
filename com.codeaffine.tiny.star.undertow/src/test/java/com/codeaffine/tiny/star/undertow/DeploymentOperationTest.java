package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.MULTI_ENTRYPOINT_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentOperationTest {

    @TempDir
    private File workingDirectory;

    @Test
    void deployRwtApplication() {
        DeploymentOperation deploymentOperation = new DeploymentOperation(MULTI_ENTRYPOINT_CONFIGURATION, workingDirectory);
        ServletInfoFactory servletInfoFactory = new ServletInfoFactory(MULTI_ENTRYPOINT_CONFIGURATION);
        ServletInfo servletInfo = servletInfoFactory.createRwtServletInfo();

        DeploymentManager actual = deploymentOperation.deployRwtApplication(servletInfo);

        DeploymentInfo deploymentInfo = actual.getDeployment().getDeploymentInfo();
        FileResourceManager resourceManager = (FileResourceManager) deploymentInfo.getResourceManager();
        assertThat(deploymentInfo.getContextPath()).isEqualTo(DeploymentOperation.CONTEXT_PATH);
        assertThat(deploymentInfo.getDeploymentName()).isEqualTo(DeploymentOperation.DEPLOYMENT_NAME);
        assertThat(deploymentInfo.getClassLoader()).isSameAs(MULTI_ENTRYPOINT_CONFIGURATION.getClass().getClassLoader());
        assertThat(deploymentInfo.getServlets())
            .hasSize(1)
            .allSatisfy((key, value) -> assertThat(key).isEqualTo(servletInfo.getName()))
            .allSatisfy((key, value) -> assertThat(value.getName()).isEqualTo(servletInfo.getName()));
        assertThat(deploymentInfo.getDeploymentCompleteListeners())
            .hasSize(1)
            .allSatisfy(listener -> assertThat(listener).isInstanceOf(TinyStarServletContextListener.class));
        assertThat(resourceManager.getBase()).isEqualTo(workingDirectory);
    }

    @Test
    void deployRwtApplicationWithNullAsArgumentNameArgument() {
        DeploymentOperation deploymentOperation = new DeploymentOperation(MULTI_ENTRYPOINT_CONFIGURATION, workingDirectory);

        assertThatThrownBy(() -> deploymentOperation.deployRwtApplication(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationConfigurationArgument() {
        assertThatThrownBy(() -> new DeploymentOperation(null, workingDirectory))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsWorkingDirectoryArgument() {
        assertThatThrownBy(() -> new DeploymentOperation(MULTI_ENTRYPOINT_CONFIGURATION, null))
            .isInstanceOf(NullPointerException.class);
    }
}
