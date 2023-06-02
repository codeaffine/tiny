/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.codeaffine.tiny.star.spi.Protocol.HTTP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ApplicationServerConfigurationTest {

    private static final File WORKING_DIRECTORY = new File("workingDirectory");
    private static final ApplicationConfiguration APPLICATION_CONFIGURATION
        = application -> application.addEntryPoint("/app", (Class<? extends EntryPoint>) null, null);
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    @Test
    void construct() {
        ApplicationServer server = ApplicationServer.newApplicationServerBuilder(APPLICATION_CONFIGURATION)
            .withHost(HOST)
            .withPort(PORT)
            .build();
        ApplicationServerConfiguration actual = new ApplicationServerConfiguration(WORKING_DIRECTORY, server);

        assertThat(actual.getWorkingDirectory()).isEqualTo(WORKING_DIRECTORY);
        assertThat(actual.getProtocol()).isEqualTo(HTTP);
        assertThat(actual.getHost()).isEqualTo(HOST);
        assertThat(actual.getPort()).isEqualTo(PORT);
        assertThat(actual.getContextClassLoader()).isEqualTo(APPLICATION_CONFIGURATION.getClass().getClassLoader());
        assertThat(actual.getContextListener()).isInstanceOf(TinyStarServletContextListener.class);
        assertThat(actual.getEntryPointPaths()).containsExactly("/app");
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
