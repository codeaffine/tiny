package com.codeaffine.tiny.star.tomcat;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.RwtServletContextListenerAdapter;
import com.codeaffine.tiny.star.spi.Server;

import jakarta.servlet.ServletContextEvent;
import java.io.File;
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

    private Tomcat tomcat;

    ServerImpl(int port, String host, File workingDirectory, ApplicationConfiguration applicationConfiguration) {
        this(port, host, workingDirectory, applicationConfiguration, getLogger(ServerImpl.class));
    }

    @Override
    public void start() {
        tomcat = new Tomcat();
        tomcat.setBaseDir(workingDirectory.getAbsolutePath());
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.getService().addConnector(connector);

        String contextPath = "";
        File docBase = new File(workingDirectory, "doc-base");
        docBase.mkdir();
        Context context = tomcat.addContext(contextPath, docBase.getAbsolutePath());

        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/", "default");

        tomcat.addServlet(contextPath, "rwtServlet", new RwtServletAdapter());
        Set<String> entrypointPaths = captureEntrypointPaths(applicationConfiguration);
        entrypointPaths.forEach(path -> context.addServletMappingDecoded(path + "/*", "rwtServlet"));
        RwtServletContextListenerAdapter rwtServletContextListenerAdapter = new RwtServletContextListenerAdapter();
        context.addServletContainerInitializer(
            (classes, servletContext) -> rwtServletContextListenerAdapter.contextInitialized(new ServletContextEvent(servletContext)),
            null);
        context.addParameter("org.eclipse.rap.applicationConfiguration", applicationConfiguration.getClass().getName());
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
        entrypointPaths.forEach(path -> logger.atInfo().log("Application Entrypoint URL:  http://{}:{}{}", host, port, path));
    }

    @Override
    public void stop() {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        return "Tomcat";
    }
}
