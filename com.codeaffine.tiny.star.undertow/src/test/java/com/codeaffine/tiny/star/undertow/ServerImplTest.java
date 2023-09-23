/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ServerImplTest {

    private static final ServletInfo SERVLET_INFO = mock(ServletInfo.class);
    private static final DeploymentManager DEPLOYMENT_MANAGER = mock(DeploymentManager.class);
    private static final PathHandler PATH_HANDLER = mock(PathHandler.class);

    private DeploymentOperation deploymentOperation;
    private ServletInfoFactory servletInfoFactory;
    private HttpHandlerStarter httpHandlerStarter;
    private UndertowLifecycle undertowLifecycle;
    private ServerImpl server;

    @BeforeEach
    void setUp() {
        servletInfoFactory = stubServletInfoFactory();
        deploymentOperation = stubDeploymentOperation();
        httpHandlerStarter = stubHttpHandlerStarter();
        undertowLifecycle = mock(UndertowLifecycle.class);
        server = new ServerImpl(servletInfoFactory, deploymentOperation, httpHandlerStarter, undertowLifecycle);
    }

    @Test
    void getName() {
        String actual = server.getName();

        assertThat(actual).isEqualTo(Texts.SERVER_NAME);
    }

    @Test
    void start() {
        server.start();

        verify(servletInfoFactory).createRwtServletInfo();
        verify(deploymentOperation).deployRwtApplication(SERVLET_INFO);
        verify(httpHandlerStarter).startRwtApplicationHttpHandler(DEPLOYMENT_MANAGER);
        verify(undertowLifecycle).startUndertow(PATH_HANDLER, DEPLOYMENT_MANAGER);
    }

    @Test
    void stop() {
        server.stop();

        verify(undertowLifecycle).stopUndertow();
    }

    private static ServletInfoFactory stubServletInfoFactory() {
        ServletInfoFactory result = mock(ServletInfoFactory.class);
        when(result.createRwtServletInfo()).thenReturn(SERVLET_INFO);
        return result;
    }

    private static DeploymentOperation stubDeploymentOperation() {
        DeploymentOperation result = mock(DeploymentOperation.class);
        when(result.deployRwtApplication(SERVLET_INFO)).thenReturn(DEPLOYMENT_MANAGER);
        return result;
    }

    private static HttpHandlerStarter stubHttpHandlerStarter() {
        HttpHandlerStarter result = mock(HttpHandlerStarter.class);
        when(result.startRwtApplicationHttpHandler(DEPLOYMENT_MANAGER)).thenReturn(PATH_HANDLER);
        return result;
    }
}
