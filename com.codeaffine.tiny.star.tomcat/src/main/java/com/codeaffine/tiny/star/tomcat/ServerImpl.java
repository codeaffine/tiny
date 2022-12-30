package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.Server;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.io.File;

import static com.codeaffine.tiny.star.tomcat.Texts.SERVER_NAME;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ServerImpl implements Server {

    @NonNull
    private final ContextRegistrar contextRegistrar;
    @NonNull
    private final ConnectorRegistrar connectorRegistrar;
    @NonNull
    private final ResourcesServletRegistrar resourcesServletRegistrar;
    @NonNull
    private final RwtServletRegistrar rwtServletRegistrar;
    @NonNull
    private final TomcatLifeCycleControl tomcatLifeCycleControl;

    ServerImpl(int port, String host, File workingDirectory, ApplicationConfiguration applicationConfiguration) {
        this(new Tomcat(), port, host, workingDirectory, applicationConfiguration);
    }

    ServerImpl(Tomcat tomcat, int port, String host, File workingDirectory, ApplicationConfiguration applicationConfiguration) {
        this(
            new ContextRegistrar(workingDirectory, tomcat),
            new ConnectorRegistrar(tomcat, host, port),
            new ResourcesServletRegistrar(),
            new RwtServletRegistrar(tomcat, applicationConfiguration),
            new TomcatLifeCycleControl(tomcat)
        );
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void start() {
        connectorRegistrar.addConnector();
        Context context = contextRegistrar.addContext();
        resourcesServletRegistrar.addResourcesServlet(context);
        rwtServletRegistrar.addRwtServlet(context);
        tomcatLifeCycleControl.startTomcat();
    }

    @Override
    public void stop() {
        tomcatLifeCycleControl.stopTomcat();
    }
}
