/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import com.codeaffine.tiny.star.spi.ServerFactory;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class ApplicationServerTestContext implements ServerFactory, Server, InvocationInterceptor {

    static final AtomicReference<ApplicationServerTestContext> CURRENT_SERVER = new AtomicReference<>();
    static final String TEST_SERVER = "TEST-SERVER";

    private ServerConfiguration configuration;
    private boolean started;
    private boolean stopped;

    public ApplicationServerTestContext() {
        CURRENT_SERVER.set(this);
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public String getName() {
        return TEST_SERVER;
    }

    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext)
        throws Throwable
    {
        CURRENT_SERVER.set(null);
        try {
            invocation.proceed();
        } finally {
            CURRENT_SERVER.set(null);
        }
    }

    @Override
    public Server create(ServerConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public static ServerConfiguration getCurrentServerConfiguration() {
        return CURRENT_SERVER.get().getConfiguration();
    }
}
