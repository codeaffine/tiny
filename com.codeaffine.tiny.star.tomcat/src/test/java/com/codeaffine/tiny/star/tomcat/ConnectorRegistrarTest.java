/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.util.function.Supplier;

import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class ConnectorRegistrarTest {

    @Test
    void addConnector() {
        Tomcat tomcat = stubTomcatWithServiceSpy(Tomcat::new);
        ConnectorRegistrar connectorRegistrar = new ConnectorRegistrar(tomcat, CONFIGURATION);
        
        connectorRegistrar.addConnector();

        ArgumentCaptor<Connector> captor = forClass(Connector.class);
        verify(tomcat.getService()).addConnector(captor.capture());
        assertThat(captor.getValue().getPort()).isEqualTo(PORT);
        assertThat(captor.getValue().getScheme()).isEqualTo("http");
    }

    @Test
    void addConnectorWithSslConfigured() {
        Tomcat tomcat = stubTomcatWithServiceSpy(Tomcat::new);
        InputStream keyStore = getClass().getClassLoader().getResourceAsStream("tiny.jks");
        SecureSocketLayerConfiguration sslConfiguration = new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD);
        ServerConfiguration configuration = stubServerConfiguration(HOST, PORT, sslConfiguration);
        ConnectorRegistrar connectorRegistrar = new ConnectorRegistrar(tomcat, configuration);

        connectorRegistrar.addConnector();

        ArgumentCaptor<Connector> captor = forClass(Connector.class);
        verify(tomcat.getService()).addConnector(captor.capture());
        assertThat(captor.getValue().getPort()).isEqualTo(PORT);
        assertThat(captor.getValue().getScheme()).isEqualTo(SslConnectorConfigurator.SCHEME);
    }

    @Test
    void constructWithNullAsTomcatArgument() {
        assertThatThrownBy(() -> new ConnectorRegistrar(null, CONFIGURATION))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new ConnectorRegistrar(mock(Tomcat.class), null))
            .isInstanceOf(NullPointerException.class);
    }

    private static Tomcat stubTomcatWithServiceSpy(Supplier<Tomcat> tomcatSupplier) {
        Tomcat result = spy(tomcatSupplier.get());
        Service service = spy(result.getService());
        when(result.getService()).thenReturn(service);
        return result;
    }
}
