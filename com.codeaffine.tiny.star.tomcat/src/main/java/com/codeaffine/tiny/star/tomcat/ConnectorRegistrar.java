package com.codeaffine.tiny.star.tomcat;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ConnectorRegistrar {

    @NonNull
    private final Tomcat tomcat;
    @NonNull
    private final String host;
    private final int port;

    void addConnector() {
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.getHost().setName(host);
        tomcat.getService().addConnector(connector);
    }
}
