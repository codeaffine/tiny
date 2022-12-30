package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import com.codeaffine.tiny.star.spi.Server;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.util.Set;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

@RequiredArgsConstructor(access = PACKAGE)
class ServerImpl implements Server {

    private final int port;
    @NonNull
    private final String host;
    @NonNull
    private final File workingDirectory;
    @NonNull
    private final ApplicationConfiguration applicationConfiguration;
    @NonNull
    private final Logger logger;

    private Undertow server;

    ServerImpl(int port, String host, File workingDirectory, ApplicationConfiguration applicationConfiguration) {
        this(port, host, workingDirectory, applicationConfiguration, getLogger(ServerImpl.class));
    }

    @Override
    public void start() {
        ServletInfo servletInfo = Servlets.servlet("rwtServlet", RwtServletAdapter.class);
        Set<String> entrypointPaths = captureEntrypointPaths(applicationConfiguration);
        entrypointPaths.forEach(path -> servletInfo.addMapping(path + "/*"));
        DeploymentInfo servletBuilder = Servlets.deployment()
            .setClassLoader(applicationConfiguration.getClass().getClassLoader())
            .setContextPath("")
            .addDeploymentCompleteListener(new TinyStarServletContextListener(applicationConfiguration))
            .setDeploymentName("application.war")
            .addServlets(servletInfo)
            .setResourceManager(new FileResourceManager(workingDirectory, 1));

        var manager = Servlets.defaultContainer()
            .addDeployment(servletBuilder);

        manager.deploy();

        HttpHandler httpHandler;
        try {
            httpHandler = manager.start();
        } catch (ServletException cause) {
            throw new IllegalStateException(cause);
        }
        PathHandler path = Handlers.path()
            .addPrefixPath("/", httpHandler);

        server = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(path)
            .build();

        server.start();
    }

    @Override
    public void stop() {
        server.stop();
    }

    @Override
    public String getName() {
        return "Undertow";
    }
}
