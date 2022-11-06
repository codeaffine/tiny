package com.codeaffine.tiny.star.undertow;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeaffine.tiny.star.EntrypointPathCaptor;
import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.RwtServletContextListenerAdapter;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
            .addInitParameter("org.eclipse.rap.applicationConfiguration", applicationConfiguration.getClass().getName())
            .addDeploymentCompleteListener(new RwtServletContextListenerAdapter())
            .setDeploymentName("application.war")
            .addServlets(servletInfo)
            .setResourceManager(new FileResourceManager(workingDirectory, 1));

        var manager = Servlets.defaultContainer()
            .addDeployment(servletBuilder);

        manager.deploy();

        HttpHandler servletHandler = null;
        try {
            servletHandler = manager.start();
        } catch (ServletException cause) {
            throw new IllegalStateException(cause);
        }
        PathHandler path = Handlers.path()
            .addPrefixPath("/", servletHandler);

        server = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(path)
            .build();

        server.start();
        entrypointPaths.forEach(entrypointPath-> logger.atInfo().log("Application Entrypoint URL:  http://{}:{}{}", host, port, entrypointPath));
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
