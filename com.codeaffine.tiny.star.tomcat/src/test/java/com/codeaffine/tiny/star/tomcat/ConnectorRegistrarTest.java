/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class ConnectorRegistrarTest {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    @Test
    void addConnector() {
        Tomcat tomcat = stubTomcatWithServiceSpy(Tomcat::new);
        ConnectorRegistrar connectorRegistrar = new ConnectorRegistrar(tomcat, HOST, PORT);
        
        connectorRegistrar.addConnector();

        ArgumentCaptor<Connector> captor = forClass(Connector.class);
        verify(tomcat.getService()).addConnector(captor.capture());
        assertThat(captor.getValue().getPort()).isEqualTo(PORT);
    }

    @Test
    void constructWithNullAsTomcatArgument() {
        assertThatThrownBy(() -> new ConnectorRegistrar(null, HOST, PORT))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsHostArgument() {
        assertThatThrownBy(() -> new ConnectorRegistrar(mock(Tomcat.class), null, PORT))
            .isInstanceOf(NullPointerException.class);
    }

    private static Tomcat stubTomcatWithServiceSpy(Supplier<Tomcat> tomcatSupplier) {
        Tomcat result = spy(tomcatSupplier.get());
        Service service = spy(result.getService());
        when(result.getService()).thenReturn(service);
        return result;
    }

}
