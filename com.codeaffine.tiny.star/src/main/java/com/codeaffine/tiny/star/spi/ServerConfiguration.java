/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import jakarta.servlet.ServletContextListener;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface ServerConfiguration {

    Protocol getProtocol();
    String getHost();
    int getPort();
    ClassLoader getContextClassLoader();
    ServletContextListener getContextListener();
    File getWorkingDirectory();
    Set<String> getEntryPointPaths();
    List<FilterDefinition> getFilterDefinitions();
}
