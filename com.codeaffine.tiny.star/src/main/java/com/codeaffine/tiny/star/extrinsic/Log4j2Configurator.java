package com.codeaffine.tiny.star.extrinsic;

import static com.codeaffine.tiny.star.common.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.extrinsic.Messages.ERROR_UNABLE_TO_CONFIGURE_LOG4J2;
import static com.codeaffine.tiny.star.extrinsic.Messages.LOG_LOG4J2_CONFIGURATION_NOT_FOUND;
import static com.codeaffine.tiny.star.extrinsic.Messages.LOG_LOG4J2_DETECTED;
import static com.codeaffine.tiny.star.extrinsic.Messages.LOG_LOG4J2_RECONFIGURATION_SUCESSFUL;
import static com.codeaffine.tiny.star.extrinsic.Messages.LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER;
import static com.codeaffine.tiny.star.extrinsic.Messages.LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER;
import static com.codeaffine.tiny.star.extrinsic.Messages.LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_SYSTEM_CLASS_LOADER;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import com.codeaffine.tiny.star.ApplicationRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
public class Log4j2Configurator implements Runnable {

    static final String CONFIGURATION_FILE_NAME_SUFFIX = "-log4j2.xml";

    private static final String LOG4J_CONFIGURATOR_CLASS = "org.apache.logging.log4j.core.config.Configurator";
    private static final String RECONFIGURE_METHOD = "reconfigure";

    @NonNull
    private final ApplicationConfiguration configuration;
    @NonNull
    private final String applicationName;
    @NonNull
    private final String configuratorClassName;
    @NonNull
    private final String reconfigureMethod;
    @NonNull
    private final Function<String, URL> systemResourceResolver;
    @NonNull
    private final Logger logger;

    public Log4j2Configurator(ApplicationConfiguration configuration, String applicationName) {
        this(configuration, applicationName, LOG4J_CONFIGURATOR_CLASS, RECONFIGURE_METHOD, ClassLoader::getSystemResource, getLogger(Log4j2Configurator.class));
    }

    @Override
    public void run() {
        Class<?> configuratorClass = loadConfiguratorClass();
        if(nonNull(configuratorClass)) {
            String configurationFileName = applicationName + CONFIGURATION_FILE_NAME_SUFFIX;
            logger.debug(LOG_LOG4J2_DETECTED, configurationFileName);
            configure(configuratorClass, configurationFileName);
        }
    }

    private Class<?> loadConfiguratorClass() {
        ClassLoader applicationLoader = ApplicationRunner.class.getClassLoader();
        try {
            return applicationLoader.loadClass(configuratorClassName);
        } catch (Exception cause) {
            return null;
        }
    }

    private void configure(Class<?> configuratorClass, String configurationFileName) throws IllegalStateException {
        URL resource = loadConfiguration(configurationFileName);
        if(isNull(resource)) {
            logger.debug(LOG_LOG4J2_CONFIGURATION_NOT_FOUND, configurationFileName); // NOSONAR
        } else {
            configure(configuratorClass, resource);
            logger.debug(LOG_LOG4J2_RECONFIGURATION_SUCESSFUL, configurationFileName);
        }
    }

    private URL loadConfiguration(String configurationFileName) {
        URL result = null;
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        if (nonNull(contextClassLoader)) {
            logger.debug(LOG_TRY_LOADING_CONFIGURATION_LOADING_FROM_CONTEXT_CLASS_LOADER, configurationFileName);
            result = contextClassLoader.getResource(configurationFileName);
        }
        if (isNull(result)) {
            logger.debug(LOG_TRY_LOADING_CONFIGURATION_FROM_APPLICATION_CLASS_LOADER, configurationFileName);
            ClassLoader applicationClassLoader = configuration.getClass().getClassLoader();
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
            throw extractExceptionToReport(cause, throwable -> new IllegalStateException(ERROR_UNABLE_TO_CONFIGURE_LOG4J2, throwable));
        }
    }
}
