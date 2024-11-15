/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ApplicationRunner;

@RequiredArgsConstructor
public class TinyStarServletContextListener implements ServletContextListener {

    @NonNull
    private final ApplicationConfiguration configuration;

    private ApplicationRunner applicationRunner;

    @Override
    public void contextInitialized( ServletContextEvent event ) {
        ServletContext servletContext = event.getServletContext();
        applicationRunner = new ApplicationRunner(configuration, servletContext);
        applicationRunner.start();
    }

    @Override
    public void contextDestroyed( ServletContextEvent event ) {
        applicationRunner.stop();
        applicationRunner = null;
    }
}
