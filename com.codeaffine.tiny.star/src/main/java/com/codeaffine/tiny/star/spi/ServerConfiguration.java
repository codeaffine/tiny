/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpServlet;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Configuration for the server.
 * <p>
 * The configuration is used to initialize the embedded servlet container.
 * </p>
 */
public interface ServerConfiguration {

    /**
     * provides a particular SecureSocketLayerConfiguration in case the server must be started with SSL support.
     *
     * @return a particular SecureSocketLayerConfiguration in case the server
     * must be started with SSL support.
     */
    SecureSocketLayerConfiguration getSecureSocketLayerConfiguration();

    /**
     * provides the host name or IP address of the server.
     *
     * @return the host name or IP address of the server.
     */
    String getHost();

    /**
     * provides the port number of the server.
     *
     * @return the port number of the server.
     */
    int getPort();

    /**
     * provides the context class loader used to load the RWT application's classes.
     *
     * @return the context class loader used to load the RWT application's classes.
     */
    ClassLoader getContextClassLoader();

    /**
     * provides the directory on the file system that is used as the working directory for
     * the server.
     *
     * @return the directory on the file system that is used as the working directory for
     * the server.
     */
    File getWorkingDirectory();

    /**
     * provides a set of paths provided by the RWT application configuration that define
     * the entry points of the RWT application run by the server.
     * @return a set of paths provided by the RWT application configuration that define
     * the entry points of the RWT application run by the server.
     */
    Set<String> getEntryPointPaths();

    /**
     * provides a list of filter definitions that will be registered with the servlet.
     *
     * @return a list of filter definitions that will be registered with the servlet.
     */
    List<FilterDefinition> getFilterDefinitions();

    /**
     * provides the user session timeout in minutes.
     *
     * @return the user session timeout in minutes.
     */
    int getSessionTimeout();

    /**
     * provides the RWT Application's servlet context listener that will be registered with the servlet.
     *
     * @return the RWT Application's servlet context listener that will be registered with the servlet.
     */
    ServletContextListener getContextListener();

    /**
     * provides the servlet class that will be used to handle the RWT application requests. Usually,
     * this is the RWT library's servlet class, a subclass of it or wrapper around it. The class
     * must provide a parameterless public constructor for instantiation.
     *
     * @return the servlet class that will be used to handle the RWT application requests.
     * @param <T> the type of the servlet class
     */
    <T extends HttpServlet> Class<T> getHttpServletClass();
}
