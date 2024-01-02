/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import com.codeaffine.tiny.star.ApplicationServer;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;

class ApplicationServerContext {

    private final List<Consumer<ApplicationServer>> startObservers;
    private final List<Consumer<ApplicationServer>> stopObservers;
    private final ApplicationConfiguration configuration;

    private ApplicationServer applicationServer;

    ApplicationServerContext(ApplicationConfiguration configuration) {
        this.startObservers = new CopyOnWriteArrayList<>();
        this.stopObservers = new CopyOnWriteArrayList<>();
        this.configuration = configuration;
        initialize();
    }

    void startServer() {
        applicationServer.start();
    }

    void stopServer() {
        applicationServer.stop();
    }

    State getState() {
        return applicationServer.getState();
    }

    String getUrl() {
        return applicationServer.getUrls()[0].toString();
    }

    void addServerStartedListener(Consumer<ApplicationServer> observer) {
        startObservers.add(observer);
    }

    void removeServerStartedListener(Consumer<ApplicationServer> observer) {
        startObservers.remove(observer);
    }

    void addServerStoppedListener(Consumer<ApplicationServer> observer) {
        stopObservers.add(observer);
    }

    void removeServerStoppedListener(Consumer<ApplicationServer> observer) {
        stopObservers.remove(observer);
    }

    @Started
    void onServerStarted() {
        startObservers.forEach(observer -> observer.accept(applicationServer));
    }

    @Stopped
    void onServerStopped() {
        stopObservers.forEach(observer -> observer.accept(applicationServer));
    }

    private void initialize() {
        applicationServer = newApplicationServerBuilder(configuration, configuration.getClass().getName())
            .withStartInfoProvider(null)
            .withLifecycleListener(this)
            .build();
    }
}
