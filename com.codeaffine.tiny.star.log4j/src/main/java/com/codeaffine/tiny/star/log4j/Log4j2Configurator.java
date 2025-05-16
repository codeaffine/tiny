/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.star.ApplicationServer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;

import static com.codeaffine.tiny.shared.Reflections.ExceptionExtractionMode.FORWARD_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.log4j.Texts.*;
import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

@RequiredArgsConstructor(access = PACKAGE)
class Log4j2Configurator {

    static final String CONFIGURATION_FILE_NAME_SUFFIX = "-log4j2.xml";

    @SuppressWarnings("CanBeFinal")
    static Logger logger = getLogger(Log4j2Configurator.class);

    private static final String LOG4J_CONFIGURATOR_CLASS = "org.apache.logging.log4j.core.config.Configurator";
    private static final String RECONFIGURE_METHOD = "reconfigure";

    private final String configuratorClassName;
    @NonNull
    private final String reconfigureMethod;
    @NonNull
    private final Function<String, URL> systemResourceResolver;

    Log4j2Configurator() {
        this(LOG4J_CONFIGURATOR_CLASS, RECONFIGURE_METHOD, ClassLoader::getSystemResource);
    }

    void run(@NonNull ClassLoader applicationClassLoader, @NonNull String applicationName) {
        Class<?> configuratorClass = loadConfiguratorClass();
        if(nonNull(configuratorClass)) {
            String configurationFileName = applicationName + CONFIGURATION_FILE_NAME_SUFFIX;
            logger.debug(LOG_LOG4J2_DETECTED, configurationFileName);
            configure(configuratorClass, configurationFileName, applicationClassLoader);
        }
    }

    private Class<?> loadConfiguratorClass() {
        ClassLoader applicationLoader = ApplicationServer.class.getClassLoader();
        try {
            return applicationLoader.loadClass(configuratorClassName);
        } catch (Exception cause) {
            return null;
        }
    }

    private void configure(Class<?> configuratorClass, String configurationFileName, ClassLoader applicationClassLoader) throws IllegalStateException {
        URL resource = loadConfiguration(configurationFileName, applicationClassLoader);
        if(isNull(resource)) {
            logger.debug(LOG_LOG4J2_CONFIGURATION_NOT_FOUND, configurationFileName); // NOSONAR
        } else {
            configure(configuratorClass, resource);
            logger.debug(LOG_LOG4J2_RECONFIGURATION_SUCCESSFUL, configurationFileName);
        }
    }

    private URL loadConfiguration(String configurationFileName, ClassLoader applicationClassLoader) {
        URL result = null;
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        if (nonNull(contextClassLoader)) {
            logger.debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER, configurationFileName);
            result = contextClassLoader.getResource(configurationFileName);
        }
        if (isNull(result)) {
            logger.debug(LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER, configurationFileName);
            result = applicationClassLoader.getResource(configurationFileName);
        }
        if(isNull(result)) {
            logger.debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_SYSTEM_CLASS_LOADER, configurationFileName);
            result = systemResourceResolver.apply(configurationFileName);
        }
        return result;
    }

    private void configure(Class<?> configuratorClass, URL resource) {
        try {
            Method method = configuratorClass.getMethod(reconfigureMethod, URI.class);
            method.invoke(null, resource.toURI());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | URISyntaxException cause) {
            throw extractExceptionToReport(
                cause,
                throwable -> new IllegalStateException(ERROR_UNABLE_TO_CONFIGURE_LOG4J2, throwable),
                FORWARD_RUNTIME_EXCEPTIONS
            );
        }
    }
}
