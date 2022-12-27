package com.codeaffine.tiny.star;

import org.junit.jupiter.api.extension.*;

import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static org.junit.jupiter.api.extension.ExtensionContext.*;

public class ApplicationServerContractContext implements ParameterResolver, BeforeAllCallback {

    static final Namespace NAMESPACE = Namespace.create(ApplicationServerCompatibilityContract.class);
    static final String CONTEXT_STORAGE_KEY = ApplicationServerContractContext.class.getName();

    private final AtomicReference<ApplicationServer> applicationServerHolder;

    ApplicationServerContractContext() {
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
}
