/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import com.codeaffine.tiny.star.ApplicationServer;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

class LaunchPadApplicationServerObserver {

    private final ServerPushSession serverPushSession;
    private final Label applicationServerStatusLabel;
    private final Button controlButton;
    private final UrlLauncher urlLauncher;
    private final Display display;
    private final Link link;

    private Listener listener;

    LaunchPadApplicationServerObserver(
        Label applicationServerStatusLabel,
        Button controlButton,
        Link link,
        ServerPushSession serverPushSession,
        UrlLauncher urlLauncher)
    {
        this.applicationServerStatusLabel = applicationServerStatusLabel;
        this.controlButton = controlButton;
        this.link = link;
        this.serverPushSession = serverPushSession;
        this.urlLauncher = urlLauncher;
        this.display = Display.getCurrent();
    }

    void applicationStarted(ApplicationServer applicationServer) {
        display.asyncExec(() -> {
            String url = applicationServer.getUrls()[0].toString();
            applicationServerStatusLabel.setText(LaunchPad.getApplicationServerStatusInfo(applicationServer.getState()));
            controlButton.setText(LaunchPad.getApplicationServerActionLabel(applicationServer.getState()));
            link.setText(LaunchPad.getApplicationServerUrlLink(applicationServer.getState(), url));
            if (isNull(listener)) {
                this.listener = evt -> urlLauncher.openURL(url);
            }
            link.addListener(SWT.Selection, listener);
            controlButton.getShell().layout();
            serverPushSession.stop();
        });
    }

    void applicationStopped(ApplicationServer applicationServer) {
        display.asyncExec(() -> {
            applicationServerStatusLabel.setText(LaunchPad.getApplicationServerStatusInfo(applicationServer.getState()));
            controlButton.setText(LaunchPad.getApplicationServerActionLabel(applicationServer.getState()));
            link.setText(LaunchPad.getApplicationServerUrlLink(applicationServer.getState(), applicationServer.getUrls()[0].toString()));
            if (nonNull(listener)) {
                link.removeListener(SWT.Selection, listener);
            }
            controlButton.getShell().layout();
            serverPushSession.stop();
        });
    }
}
