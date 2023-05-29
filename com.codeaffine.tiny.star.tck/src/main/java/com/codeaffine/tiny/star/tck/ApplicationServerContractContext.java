/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import com.codeaffine.tiny.star.ApplicationServer;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.ApplicationServer.Starting;
import static com.codeaffine.tiny.star.ApplicationServer.Stopping;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import static org.junit.jupiter.api.extension.ExtensionContext.Store;

public class ApplicationServerContractContext implements ParameterResolver, BeforeAllCallback, EntryPointFactory {

    static final Namespace NAMESPACE = Namespace.create(ApplicationServerCompatibilityContract.class);
    static final String CONTEXT_STORAGE_KEY = ApplicationServerContractContext.class.getName();

    private final AtomicReference<EntryPointFactory> entryPointFactoryHub;
    private final AtomicReference<ApplicationServer> applicationServerHolder;
    @Setter
    @Getter
    private File workingDirectory;

    ApplicationServerContractContext() {
        entryPointFactoryHub = new AtomicReference<>();
        applicationServerHolder = new AtomicReference<>();
    }

    @Starting
    void startingApplicationServer(ApplicationServer applicationServer) {
        applicationServerHolder.set(applicationServer);
    }

    @Stopping
    void stoppingApplicationServer() {
        applicationServerHolder.set(null);
    }

    ApplicationServer getApplicationServer() {
        return applicationServerHolder.get();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        return getClass() == parameterContext.getParameter().getType();
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        Store store = extensionContext.getStore(NAMESPACE);
        return store.get(CONTEXT_STORAGE_KEY, ApplicationServerContractContext.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getStore(NAMESPACE).put(CONTEXT_STORAGE_KEY, this);
    }

    @Override
    public EntryPoint create() {
        return entryPointFactoryHub.get().create();
    }

    UserSession simulateUserSession(URL url) {
        String uuid = UUID.randomUUID().toString();
        return new UserSession(url, entryPointFactoryHub, uuid);
    }

    void configure(Application application) {
        application.addEntryPoint("/ui", this, null);
    }
}
