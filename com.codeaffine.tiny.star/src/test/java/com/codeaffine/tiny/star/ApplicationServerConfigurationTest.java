/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.codeaffine.tiny.star.spi.ServerConfigurationAssert;
import jakarta.servlet.Filter;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ApplicationServerConfigurationTest {

    private static final File WORKING_DIRECTORY = new File("workingDirectory");
    private static final ApplicationConfiguration APPLICATION_CONFIGURATION
        = application -> application.addEntryPoint("/app", (Class<? extends EntryPoint>) null, null);
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final SecureSocketLayerConfiguration SECURE_SOCKET_LAYER_CONFIGURATION = mock(SecureSocketLayerConfiguration.class);
    private static final int SESSION_TIMEOUT = 42;

    @Test
    void construct() {
        FilterDefinition filterDefinition = FilterDefinition.of(mock(Filter.class));
        ApplicationServer server = ApplicationServer.newApplicationServerBuilder(APPLICATION_CONFIGURATION)
            .withHost(HOST)
            .withPort(PORT)
            .withFilterDefinition(filterDefinition)
            .withSecureSocketLayerConfiguration(SECURE_SOCKET_LAYER_CONFIGURATION)
            .withSessionTimeout(SESSION_TIMEOUT)
            .build();
        ApplicationServerConfiguration actual = new ApplicationServerConfiguration(WORKING_DIRECTORY, server);

        ServerConfigurationAssert.assertThat(actual)
            .hasWorkingDirectory(WORKING_DIRECTORY)
            .hasSecureSocketLayerConfiguration(SECURE_SOCKET_LAYER_CONFIGURATION)
            .hasHost(HOST)
            .hasPort(PORT)
            .hasContextClassLoader(APPLICATION_CONFIGURATION.getClass().getClassLoader())
            .hasServletContextListenerInstanceOf(TinyStarServletContextListener.class)
            .hasEntryPointPaths(Set.of("/app"))
            .hasFilterDefinitions(List.of(filterDefinition))
            .hasSessionTimeout(SESSION_TIMEOUT);
    }

    @Test
    void constructWithNullAsWorkingDirectoryArgument() {
        assertThatThrownBy(() -> new ApplicationServerConfiguration(null, mock(ApplicationServer.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsApplicationServerArgument() {
        assertThatThrownBy(() -> new ApplicationServerConfiguration(WORKING_DIRECTORY, null))
            .isInstanceOf(NullPointerException.class);
    }
}
