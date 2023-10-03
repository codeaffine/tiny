/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import jakarta.servlet.ServletContextListener;
import org.assertj.core.api.AbstractAssert;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ServerConfigurationAssert extends AbstractAssert<ServerConfigurationAssert, ServerConfiguration> {

    public ServerConfigurationAssert(ServerConfiguration actual) {
        super(actual, ServerConfigurationAssert.class);
    }

    public static ServerConfigurationAssert assertThat(ServerConfiguration actual) {
        return new ServerConfigurationAssert(actual);
    }

    public ServerConfigurationAssert hasHost(String host) {
        isNotNull();

        if (!actual.getHost().equals(host)) {
            failWithMessage("Expected host to be <%s> but was <%s>", host, actual.getHost());
        }

        return this;
    }

    public ServerConfigurationAssert hasPort(int port) {
        isNotNull();

        if (actual.getPort() != port) {
            failWithMessage("Expected port to be <%s> but was <%s>", port, actual.getPort());
        }

        return this;
    }

    public ServerConfigurationAssert hasNonNegativePort() {
        isNotNull();

        if (actual.getPort() < 0) {
            failWithMessage("Expected port to be non-negative but was <%s>", actual.getPort());
        }

        return this;
    }

    public ServerConfigurationAssert hasContextClassLoader(ClassLoader classLoader) {
        isNotNull();

        if (!actual.getContextClassLoader().equals(classLoader)) {
            failWithMessage("Expected context class loader to be <%s> but was <%s>", classLoader, actual.getContextClassLoader());
        }

        return this;
    }

    public ServerConfigurationAssert hasContextListener(ServletContextListener contextListener) {
        isNotNull();

        if (!actual.getContextListener().equals(contextListener)) {
            failWithMessage("Expected context listener to be <%s> but was <%s>", contextListener, actual.getContextListener());
        }

        return this;
    }

    public ServerConfigurationAssert hasServletContextListenerInstanceOf(Class<? extends ServletContextListener> contextListenerClass) {
        isNotNull();

        if (!contextListenerClass.isInstance(actual.getContextListener())) {
            failWithMessage("Expected ServletContextListener to be instance of <%s> but was <%s>", contextListenerClass.getName(), actual.getContextListener().getClass().getName());
        }

        return this;
    }

    public ServerConfigurationAssert hasWorkingDirectory(File workingDirectory) {
        isNotNull();

        if (!actual.getWorkingDirectory().equals(workingDirectory)) {
            failWithMessage("Expected working directory to be <%s> but was <%s>", workingDirectory, actual.getWorkingDirectory());
        }

        return this;
    }

    public ServerConfigurationAssert hasExistingWorkingDirectory() {
        isNotNull();

        File workingDirectory = actual.getWorkingDirectory();
        if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
            failWithMessage("Expected working directory to exist and be a directory but was <%s>", workingDirectory);
        }

        return this;
    }

    public ServerConfigurationAssert hasEntryPointPaths(Set<String> entryPointPaths) {
        isNotNull();

        if (!actual.getEntryPointPaths().equals(entryPointPaths)) {
            failWithMessage("Expected entry point paths to be <%s> but was <%s>", entryPointPaths, actual.getEntryPointPaths());
        }

        return this;
    }

    public ServerConfigurationAssert hasFilterDefinitions(List<FilterDefinition> filterDefinitions) {
        isNotNull();

        if (!actual.getFilterDefinitions().equals(filterDefinitions)) {
            failWithMessage("Expected filter definitions to be <%s> but was <%s>", filterDefinitions, actual.getFilterDefinitions());
        }

        return this;
    }

    public ServerConfigurationAssert hasSessionTimeout(int sessionTimeout) {
        isNotNull();

        if (actual.getSessionTimeout() != sessionTimeout) {
            failWithMessage("Expected session timeout to be <%s> but was <%s>", sessionTimeout, actual.getSessionTimeout());
        }

        return this;
    }

    public ServerConfigurationAssert hasSecureSocketLayerConfiguration(SecureSocketLayerConfiguration secureSocketLayerConfiguration) {
        isNotNull();

        if (!actual.getSecureSocketLayerConfiguration().equals(secureSocketLayerConfiguration)) {
            failWithMessage("Expected SecureSocketLayerConfiguration to be <%s> but was <%s>", secureSocketLayerConfiguration, actual.getSecureSocketLayerConfiguration());
        }

        return this;
    }
}
