package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationRunner.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;

public class ApplicationRunnerTestContext implements ServerFactory, Server, InvocationInterceptor {

    public static final AtomicReference<ApplicationRunnerTestContext> CURRENT_SERVER = new AtomicReference<>();
    public static final String TEST_SERVER = "TEST-SERVER";

    @Getter
    private boolean started;
    @Getter
    private boolean stopped;
    @Getter
    private int port;
    @Getter
    private String host;
    @Getter
    private File workingDirectory;
    @Getter
    private ApplicationConfiguration configuration;

    public ApplicationRunnerTestContext() {
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
            System.getProperties().remove(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY);
        }
    }

    @Override
    public Server create(int port, String host, File workingDirectory, ApplicationConfiguration configuration) {
        this.port = port;
        this.host = host;
        this.workingDirectory = workingDirectory;
        this.configuration = configuration;
        return this;
    }
}
