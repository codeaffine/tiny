/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.ServerConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ConnectorRegistrar {

    @NonNull
    private final Tomcat tomcat;
    @NonNull
    private final ServerConfiguration configuration;
    @NonNull
    private SslConnectorConfigurator sslConnectorConfigurator;

    ConnectorRegistrar(Tomcat tomcat, ServerConfiguration configuration) {
        this(tomcat, configuration, new SslConnectorConfigurator(configuration));
    }

    void addConnector() {
        Connector connector = new Connector();
        connector.setPort(configuration.getPort());
        tomcat.getHost().setName(configuration.getHost());
        if (nonNull(configuration.getSecureSocketLayerConfiguration())) {
            sslConnectorConfigurator.configureSsl(connector);
        }
        tomcat.getService().addConnector(connector);
    }
}
