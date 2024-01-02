/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import com.codeaffine.tiny.star.cli.CommandLineInterface;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;

import java.util.List;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.slf4j.LoggerFactory.getLogger;

public class DemoApplication extends AbstractEntryPoint implements ApplicationConfiguration {

    private static final List<? extends ApplicationConfiguration> CONFIGURATIONS = List.of(new HelloWorld(), new FormDialog(), new ZOrder());

    private final ShutdownHandler shutdownHandler; // NOSONAR

    DemoApplication(ShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
    }

    public static void main(String[] args) {
        ShutdownHandler shutdownHandler = new ShutdownHandler();
        newApplicationServerBuilder(new DemoApplication(shutdownHandler))
            .withLifecycleListener(shutdownHandler)
            .withLifecycleListener(new CommandLineInterface())
            .build()
            .start();
    }

    @Override
    public void configure(Application application) {
        application.addEntryPoint("/ui", () -> this, null);
    }

    @Override
    protected void createContents(Composite parent) {
        shutdownHandler.registerShutdownHook();
        parent.setLayout(new FillLayout());
        LaunchPad launchPad = new LaunchPad(CONFIGURATIONS);
        launchPad.createControl(parent);
        Logger logger = getLogger(getClass());
        logger.info("Launchpad created.");
        parent.addDisposeListener(event -> getLogger(getClass()).info("Launchpad disposed."));
    }
}
