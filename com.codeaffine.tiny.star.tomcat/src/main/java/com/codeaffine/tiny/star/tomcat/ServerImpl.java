/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import static com.codeaffine.tiny.star.tomcat.Texts.SERVER_NAME;
import static java.util.Objects.nonNull;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
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
    private final FilterRegistrar filterRegistrar;
    @NonNull
    private final TomcatLifeCycleControl tomcatLifeCycleControl;
    @NonNull
    private final ServerConfiguration configuration;

    private Context context;

    ServerImpl(ServerConfiguration configuration) {
        this(new Tomcat(), configuration);
        getLogger("org.apache").setLevel(SEVERE); // use a less obtrusive logging configuration in future versions to suppress Tomcat's log output
    }

    ServerImpl(Tomcat tomcat, ServerConfiguration configuration) {
        this(
            new ContextRegistrar(configuration, tomcat),
            new ConnectorRegistrar(tomcat, configuration),
            new ResourcesServletRegistrar(),
            new RwtServletRegistrar(tomcat, configuration),
            new FilterRegistrar(configuration),
            new TomcatLifeCycleControl(tomcat),
            configuration
        );
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public void start() {
        context = contextRegistrar.addContext();
        connectorRegistrar.addConnector();
        resourcesServletRegistrar.addResourcesServlet(context);
        rwtServletRegistrar.addRwtServlet(context);
        filterRegistrar.addFilters(context);
        tomcatLifeCycleControl.startTomcat();
    }

    @Override
    public void stop() {
        if (nonNull(context)) {
            // is it really necessary to destroy the context manually? Could not find a better way to do it.
            ServletContext servletContext = context.getServletContext();
            configuration.getContextListener().contextDestroyed(new ServletContextEvent(servletContext));
            context = null;
        }
        tomcatLifeCycleControl.stopTomcat();
    }
}
