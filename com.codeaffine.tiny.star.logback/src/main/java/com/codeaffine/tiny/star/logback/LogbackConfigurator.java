/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.util.DefaultJoranConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import lombok.NonNull;

import static ch.qos.logback.classic.ClassicConstants.CONFIG_FILE_PROPERTY;
import static java.util.Objects.isNull;

public class LogbackConfigurator
    implements
        Configurator,
        LoggingFrameworkControl,
        LoggingFrameworkControlFactory
{

    static final String CONFIGURATION_FILE_NAME_SUFFIX = "-logback.xml";

    static LogbackConfigurator singleton;

    final Configurator logbackDefaultConfigurator;

    boolean configured;
    LoggerContext context;

    public LogbackConfigurator() {
        this(new DefaultJoranConfigurator());
    }

    LogbackConfigurator(@NonNull Configurator logbackDefaultConfigurator) {
        this.logbackDefaultConfigurator = logbackDefaultConfigurator;
        synchronized (LogbackConfigurator.class) {
            if(isNull(singleton)) {
                singleton = this;
            }
        }
    }

    @Override
    public LoggingFrameworkControl create() {
        return singleton;
    }

    @Override
    public ExecutionStatus configure(@NonNull LoggerContext context) {
        this.context = context;
        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }

    @Override
    public void configure(ClassLoader applicationClassLoader, @NonNull String applicationName) {
        synchronized (LogbackConfigurator.class) {
            if(!configured) {
                System.setProperty(CONFIG_FILE_PROPERTY, applicationName + CONFIGURATION_FILE_NAME_SUFFIX);
                logbackDefaultConfigurator.configure(singleton.context);
                configured = true;
            }
        }
    }

    @Override
    public void setContext(Context context) {
        singleton.logbackDefaultConfigurator.setContext(context);
    }

    @Override
    public Context getContext() {
        return singleton.logbackDefaultConfigurator.getContext();
    }

    @Override
    public void addStatus(Status status) {
        singleton.logbackDefaultConfigurator.addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        singleton.logbackDefaultConfigurator.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        singleton.logbackDefaultConfigurator.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        singleton.logbackDefaultConfigurator.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        singleton.logbackDefaultConfigurator.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        singleton.logbackDefaultConfigurator.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        singleton.logbackDefaultConfigurator.addError(msg, ex);
    }
}
