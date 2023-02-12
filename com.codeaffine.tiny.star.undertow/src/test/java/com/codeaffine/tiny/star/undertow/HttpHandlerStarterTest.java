package com.codeaffine.tiny.star.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.MULTI_ENTRYPOINT_CONFIGURATION;
import static io.undertow.Handlers.path;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpHandlerStarterTest {

    @TempDir
    private File workingDirectory;
    private DeploymentManager manager;
    private PathHandler spyPathHandler;
    private HttpHandlerStarter starter;

    @BeforeEach
    void setUp() {
        ServletInfoFactory servletInfoFactory = new ServletInfoFactory(MULTI_ENTRYPOINT_CONFIGURATION);
        DeploymentOperation deploymentOperation = new DeploymentOperation(MULTI_ENTRYPOINT_CONFIGURATION, workingDirectory);
        spyPathHandler = spy(path());
        starter = new HttpHandlerStarter(() -> spyPathHandler);
        manager = deploymentOperation.deployRwtApplication(servletInfoFactory.createRwtServletInfo());
    }

    @Test
    void startRwtApplicationHttpHandler() {
        PathHandler actual = starter.startRwtApplicationHttpHandler(manager);

        assertThat(actual).isNotNull();
        verify(spyPathHandler).addPrefixPath(eq(HttpHandlerStarter.PREFIX_PATH), any(HttpHandler.class));
    }

    @Test
    void startRwtApplicationHttpHandlerWithRuntimeProblemOnManagerStart() throws ServletException {
        RuntimeException expected = new RuntimeException("bad");
        DeploymentManager spyManager = equipDeploymentManagerStartWithProblem(expected);

        Exception actual = catchException(() -> starter.startRwtApplicationHttpHandler(spyManager));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void startRwtApplicationHttpHandlerWithCheckedProblemOnManagerStart() throws ServletException {
        ServletException expectedCause = new ServletException("bad");
        DeploymentManager spyManager = equipDeploymentManagerStartWithProblem(expectedCause);

        Exception actual = catchException(() -> starter.startRwtApplicationHttpHandler(spyManager));

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasCause(expectedCause);
    }

    @Test
    void startRwtApplicationHttpHandlerWithNullAsManagerArgument() {
        assertThatThrownBy(() -> starter.startRwtApplicationHttpHandler(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsPathHandlerFactoryArgument() {
        assertThatThrownBy(() -> new HttpHandlerStarter(null))
            .isInstanceOf(NullPointerException.class);
    }

    private DeploymentManager equipDeploymentManagerStartWithProblem(Throwable expected) throws ServletException {
        DeploymentManager result = spy(manager);
        when(result.start()).thenThrow(expected);
        return result;
    }
}
