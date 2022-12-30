package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import jakarta.servlet.ServletContextEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.util.Set;

import static com.codeaffine.tiny.star.EntrypointPathCaptor.captureEntrypointPaths;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class RwtServletRegistrar {

    static final String SERVLET_NAME = "rwtServlet";
    static final String ALL_SUB_PATHS_PATTERN = "/*";

    @NonNull
    private final Tomcat tomcat;
    @NonNull
    private final ApplicationConfiguration applicationConfiguration;
    @NonNull
    private final TinyStarServletContextListener listener;

    RwtServletRegistrar(Tomcat tomcat, ApplicationConfiguration applicationConfiguration) {
        this(tomcat, applicationConfiguration, new TinyStarServletContextListener(applicationConfiguration));
    }

    void addRwtServlet(Context context) {
        tomcat.addServlet(context.getPath(), SERVLET_NAME, new RwtServletAdapter());
        Set<String> entrypointPaths = captureEntrypointPaths(applicationConfiguration);
        entrypointPaths.forEach(path -> context.addServletMappingDecoded(path + ALL_SUB_PATHS_PATTERN, SERVLET_NAME));
        context.addServletContainerInitializer((classes, servletContext) -> listener.contextInitialized(new ServletContextEvent(servletContext)), null);
    }
}
