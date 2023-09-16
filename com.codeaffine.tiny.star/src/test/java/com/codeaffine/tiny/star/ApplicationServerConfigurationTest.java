/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import jakarta.servlet.Filter;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.junit.jupiter.api.Test;

import java.io.File;

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

    @Test
    void construct() {
        ApplicationServer server = ApplicationServer.newApplicationServerBuilder(APPLICATION_CONFIGURATION)
            .withHost(HOST)
            .withPort(PORT)
            .withFilterDefinition(FilterDefinition.of(mock(Filter.class)))
            .withSecureSocketLayerConfiguration(SECURE_SOCKET_LAYER_CONFIGURATION)
            .build();
        ApplicationServerConfiguration actual = new ApplicationServerConfiguration(WORKING_DIRECTORY, server);

        assertThat(actual.getWorkingDirectory()).isEqualTo(WORKING_DIRECTORY);
        assertThat(actual.getSecureSocketLayerConfiguration()).isSameAs(SECURE_SOCKET_LAYER_CONFIGURATION);
        assertThat(actual.getHost()).isEqualTo(HOST);
        assertThat(actual.getPort()).isEqualTo(PORT);
        assertThat(actual.getContextClassLoader()).isEqualTo(APPLICATION_CONFIGURATION.getClass().getClassLoader());
        assertThat(actual.getContextListener()).isInstanceOf(TinyStarServletContextListener.class);
        assertThat(actual.getEntryPointPaths()).containsExactly("/app");
        assertThat(actual.getFilterDefinitions()).hasSize(1);
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
