package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.Server;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.io.File;

import static com.codeaffine.tiny.star.undertow.Texts.SERVER_NAME;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ServerImpl implements Server {

    @NonNull
    private final ServletInfoFactory servletInfoFactory;
    @NonNull
    private final DeploymentOperation deploymentOperation;
    @NonNull
    private final HttpHandlerStarter httpHandlerStarter;
    @NonNull
    private final UndertowLifecycle undertowLifecycle;

    ServerImpl(int port, String host, File workingDirectory, ApplicationConfiguration applicationConfiguration) {
        this(
            new ServletInfoFactory(applicationConfiguration),
            new DeploymentOperation(applicationConfiguration, workingDirectory),
            new HttpHandlerStarter(),
            new UndertowLifecycle(host, port)
        );
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void start() {
        ServletInfo servletInfo = servletInfoFactory.createRwtServletInfo();
        DeploymentManager manager = deploymentOperation.deployRwtApplication(servletInfo);
        PathHandler path = httpHandlerStarter.startRwtApplicationHttpHandler(manager);
        undertowLifecycle.startUndertow(path);
    }

    @Override
    public void stop() {
        undertowLifecycle.stopUndertow();
    }
}
