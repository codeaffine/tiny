/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import jakarta.servlet.*;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.ApplicationServer.Starting;
import static com.codeaffine.tiny.star.ApplicationServer.Stopping;
import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.*;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import static org.junit.jupiter.api.extension.ExtensionContext.Store;
import static org.mockito.Mockito.*;

public class ApplicationServerContractContext implements ParameterResolver, BeforeAllCallback, EntryPointFactory, Filter {

    static final Namespace NAMESPACE = Namespace.create(ApplicationServerCompatibilityContractTest.class);
    static final String CONTEXT_STORAGE_KEY = ApplicationServerContractContext.class.getName();
    static final String ENTRY_POINT_PATH = "/ui"; // NOSONAR: this is a test constant

    private final AtomicReference<EntryPointFactory> entryPointFactoryHub;
    private final AtomicReference<ApplicationServer> applicationServerHolder;
    @Setter
    @Getter
    private File workingDirectory;
    @Getter
    private boolean filterInitialized;
    @Getter
    private boolean filterDestroyed;
    @Getter
    private boolean doFilterCalled;

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
        return new UserSession(url, entryPointFactoryHub, mock(Runnable.class), uuid);
    }

    void configure(Application application) {
        application.addEntryPoint(ENTRY_POINT_PATH, this, null);
    }

    SecureSocketLayerConfiguration getSecureSocketLayerConfiguration() {
        InputStream keyStore = getClass().getClassLoader().getResourceAsStream("tiny.jks");
        assert keyStore != null;
        return new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD);
    }

    void verifyServerTrustCheckInvocation(UserSession ... userSessions) {
        verifyServerTrustCheckInvocation(stream(userSessions)
            .map(UserSession::getServerTrustedCheckObserver)
            .toArray(Runnable[]::new));
    }

    void verifyServerTrustCheckInvocation(Runnable ... serverTrustedCheckObservers) {
        stream(serverTrustedCheckObservers)
            .filter(serverTrustedCheckObserver -> applicationServerHolder.get().getUrls()[0].getProtocol().equals("https"))
            .forEach(serverTrustedCheckObserver -> verify(serverTrustedCheckObserver, atLeastOnce()).run());
    }

    ///////////////////////////////////////
    // start servlet filter related methods

    @Override
    public void init(FilterConfig filterConfig) {
        filterInitialized = true;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilterCalled = true;
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        filterDestroyed = true;
    }

    // end servlet filter related methods
    ///////////////////////////////////////
}
