/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.demo.ApplicationServerContextRegistry.getApplicationServerContextRegistry;
import static com.codeaffine.tiny.demo.FormDatas.attach;
import static com.codeaffine.tiny.star.ApplicationServer.State;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

class LaunchPad {

    private static final String COLOR_LOGO_FOREGROUND = "COLOR_LOGO_FOREGROUND";
    private static final String FONT_LOGO = "FONT_LOGO";
    private static final String FONT_CONTENT_TITLE = "FONT_CONTENT_TITLE";
    private static final String FONT_CONFIGURATION_TITLE = "FONT_CONFIGURATION_TITLE";
    private static final String FONT_LABEL = "FONT_LABEL";
    private static final String DEFAULT_FONT_NAME = "Verdana, \"Lucida Sans\", Arial, Helvetica, sans-serif";
    private static final int MARGIN = 20;

    private final List<? extends ApplicationConfiguration> configurations;
    private final Map<String, Font> fonts;
    private final Map<String, Color> colors;
    private final UrlLauncher urlLauncher;
    private final Logger logger;

    LaunchPad(List<? extends ApplicationConfiguration> configurations) {
        this.configurations = configurations;
        this.fonts = Map.of(
            FONT_LOGO, new Font(Display.getCurrent(), new FontData(DEFAULT_FONT_NAME, 28, SWT.BOLD)),
            FONT_CONTENT_TITLE, new Font(Display.getCurrent(), new FontData(DEFAULT_FONT_NAME, 20, SWT.BOLD)),
            FONT_CONFIGURATION_TITLE, new Font(Display.getCurrent(), new FontData(DEFAULT_FONT_NAME, 18, SWT.NONE)),
            FONT_LABEL, new Font(Display.getCurrent(), new FontData(DEFAULT_FONT_NAME, 16, SWT.NONE))
        );
        this.colors = Map.of(
            COLOR_LOGO_FOREGROUND, new Color(Display.getCurrent(), 255, 51, 0)
        );
        this.urlLauncher = RWT.getClient().getService(UrlLauncher.class);
        logger = LoggerFactory.getLogger(getClass());
    }

    void createControl(Composite parent) {
        Composite control = newSection(parent);

        Composite banner = newSection(control);
        attach(banner).atLeft().atRight().atTop();
        Label logo = new Label(banner, SWT.NONE);
        logo.setFont(fonts.get(FONT_LOGO));
        logo.setForeground(colors.get(COLOR_LOGO_FOREGROUND));
        logo.setText("Application Launch Pad");
        attach(logo).atLeft(MARGIN).atTop(MARGIN).atBottom(MARGIN);

        Composite footer = newSection(control, new GridLayout());
        attach(footer).atLeft().atRight().atBottom().withHeight(100);
        Link copyRight = new Link(footer, SWT.NONE);
        copyRight.setText(format("© 2022-%s <a>CA Code Affine GmbH</a>", now().getYear()));
        copyRight.addListener(SWT.Selection, evt -> urlLauncher.openURL("https://codeaffine.com"));
        copyRight.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));

        Composite content = newSection(control);
        attach(content).atLeft(MARGIN).atRight().atTopTo(banner, MARGIN).atBottomTo(footer);

        Label contentTitle = new Label(content, SWT.NONE);
        contentTitle.setText("Launch Configurations");
        contentTitle.setFont(fonts.get(FONT_CONTENT_TITLE));
        attach(contentTitle).atLeft().atTop();

        Composite launchConfigurations = newSection(content);
        attach(launchConfigurations).atLeft().atRight().atTopTo(contentTitle, MARGIN).atBottom();

        configurations.forEach(configuration -> {
            Composite launchSection = newSection(launchConfigurations);
            populateLaunchSection(launchSection, configuration);
        });
        Control[] children = launchConfigurations.getChildren();
        AtomicReference<Control> previous = new AtomicReference<>();
        stream(children).forEach(launchSection -> {
            FormDatas formData = attach(launchSection).atLeft().atRight();
            if(nonNull(previous.get())) {
                formData.atTopTo(previous.get(), MARGIN);
            } else {
                formData.atTop();
            }
            previous.set(launchSection);
        });
    }

    private void populateLaunchSection(Composite launchSection, ApplicationConfiguration configuration) {
        Label applicationTitle = new Label(launchSection, SWT.NONE);
        applicationTitle.setText(configuration.getClass().getSimpleName());
        applicationTitle.setFont(fonts.get(FONT_CONFIGURATION_TITLE));
        attach(applicationTitle).atLeft().atTop(MARGIN);

        Composite applicationContent = newSection(launchSection);
        attach(applicationContent).atLeft(MARGIN).atRight().atTopTo(applicationTitle, MARGIN).atBottom().atRight();

        Label applicationServerStatus = new Label(applicationContent, SWT.NONE);
        applicationServerStatus.setFont(fonts.get(FONT_LABEL));
        attach(applicationServerStatus).atLeft().atTop().atRight();
        Link link = new Link(applicationContent, SWT.NONE);
        link.setFont(fonts.get(FONT_LABEL));
        attach(link).atLeft().atTopTo(applicationServerStatus, 4).atRight();
        Button controlButton = new Button(applicationContent, SWT.PUSH);
        controlButton.setFont(fonts.get(FONT_LABEL));
        attach(controlButton).atLeft().atTopTo(link, 4);

        ServerPushSession serverPushSession = new ServerPushSession();
        ApplicationServerContext context = getApplicationServerContextRegistry().get(configuration);
        LaunchPadApplicationServerObserver launchPadApplicationServerObserver = new LaunchPadApplicationServerObserver(applicationServerStatus, controlButton, link, serverPushSession, urlLauncher);
        context.addServerStartedListener(launchPadApplicationServerObserver::applicationStarted);
        context.addServerStoppedListener(launchPadApplicationServerObserver::applicationStopped);
        launchSection.addDisposeListener(evt -> {
            context.removeServerStartedListener(launchPadApplicationServerObserver::applicationStarted);
            context.removeServerStoppedListener(launchPadApplicationServerObserver::applicationStopped);
        });

        applicationServerStatus.setText(getApplicationServerStatusInfo(context.getState()));
        link.setText(getApplicationServerUrlLink(context.getState(), context.getUrl()));
        controlButton.setText(getApplicationServerActionLabel(context.getState()));
        controlButton.addListener(SWT.Selection, evt -> {
            serverPushSession.start();
            if(context.getState() == HALTED) {
                context.startServer();
                logger.info("Application {} started", configuration.getClass().getSimpleName());
            } else {
                context.stopServer();
                logger.info("Application {} stopped", configuration.getClass().getSimpleName());
            }
        });
    }

    private static Composite newSection(Composite control) {
        return newSection(control, new FormLayout());
    }

    private static Composite newSection(Composite control, Layout layout) {
        Composite result = new Composite(control, SWT.NONE);
        result.setLayout(layout);
        return result;
    }

    static String getApplicationServerStatusInfo(State state) {
        return format("Application Server Status: %s", state);
    }

    static String getApplicationServerActionLabel(State state) {
        return HALTED == state? "Start" : "Stop" ;
    }

    static String getApplicationServerUrlLink(State state, String url) {
        return HALTED == state? "Url: - " : "Url: <a>" + url + "</a>" ;
    }
}
