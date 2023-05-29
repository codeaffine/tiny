/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Scanner;

import static com.codeaffine.tiny.shared.IoUtils.findFreePort;
import static com.codeaffine.tiny.star.Protocol.HTTP;
import static com.codeaffine.tiny.star.undertow.DeploymentOperation.CONTEXT_PATH;
import static com.codeaffine.tiny.star.undertow.HttpHandlerStarter.PREFIX_PATH;
import static io.undertow.Handlers.path;
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static java.lang.String.format;
import static java.nio.file.Files.writeString;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UndertowLifecycleTest {

    private static final String CONNECTION_REFUSED = "Connection refused";
    private static final String INDEX_CONTENT = "content";
    private static final String HOST = "localhost";
    private static final String INDEX = "index";

    @TempDir
    private File workingDirectory;
    private UndertowLifecycle lifecycle;
    private int port;

    @BeforeEach
    void setUp() {
        port = findFreePort();
    }

    @AfterEach
    void tearDown() {
        if(nonNull(lifecycle)) {
            lifecycle.stopUndertow();
        }
    }

    @Test
    void construct() {
        assertDoesNotThrow(() -> new UndertowLifecycle(HOST, port));
    }

    @Test
    void constructWithNullAsHostArgument() {
        assertThatThrownBy(() -> new UndertowLifecycle(null, port))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    void startWithNullAsPathArgument() {
        lifecycle = new UndertowLifecycle(HOST, port);
        assertThatThrownBy(() -> lifecycle.startUndertow(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Nested
    class Started {

        @BeforeEach
        void setUp() {
            lifecycle = new UndertowLifecycle(HOST, port);
            lifecycle.startUndertow(setupBasicPathHandler());
        }

        @Test
        void requestServer() {
            String actual = readIndexContent();

            assertThat(actual).isEqualTo(INDEX_CONTENT);
        }

        @Test
        void requestServerAfterStopped() {
            lifecycle.stopUndertow();

            Exception actual = catchException(UndertowLifecycleTest.this::readIndexContent);

            assertThat(actual)
                .isInstanceOf(ConnectException.class)
                .hasMessageContaining(CONNECTION_REFUSED);
        }

        @Test
        void requestServerAfterRestartAlreadyRunningServer() {
            lifecycle.startUndertow(setupBasicPathHandler());

            String actual = readIndexContent();

            assertThat(actual).isEqualTo(INDEX_CONTENT);
        }
    }

    @Nested
    class Stopped {

        @BeforeEach
        void setUp() {
            lifecycle = new UndertowLifecycle(HOST, port);
            lifecycle.startUndertow(setupBasicPathHandler());
            lifecycle.stopUndertow();
        }

        @Test
        void requestServer() {
            Exception actual = catchException(UndertowLifecycleTest.this::readIndexContent);

            assertThat(actual)
                .isInstanceOf(ConnectException.class)
                .hasMessageContaining(CONNECTION_REFUSED);
        }

        @Test
        void requestServerAfterRestart() {
            lifecycle.startUndertow(setupBasicPathHandler());

            String actual = readIndexContent();

            assertThat(actual).isEqualTo(INDEX_CONTENT);
        }
    }

    @SneakyThrows
    private PathHandler setupBasicPathHandler() {
        File index = new File(workingDirectory, INDEX);
        if(!index.exists() && !index.createNewFile()) {
            throw new IllegalStateException(format("Unable to create file %s for basic http handler.", INDEX));
        }
        writeString(index.toPath(), INDEX_CONTENT);
        DeploymentInfo deploymentInfo = deployment()
            .setContextPath(CONTEXT_PATH)
            .setDeploymentName("application")
            .setClassLoader(getClass().getClassLoader())
            .setResourceManager(new FileResourceManager(workingDirectory, 1));
        DeploymentManager deploymentManager = defaultContainer()
            .addDeployment(deploymentInfo);
        deploymentManager.deploy();
        HttpHandler httpHandler = deploymentManager.start();
        return path()
            .addPrefixPath(PREFIX_PATH, httpHandler);
    }

    @SneakyThrows
    private String readIndexContent() {
        URI uri = new URI(HTTP.name().toLowerCase(), null, HOST, port, CONTEXT_PATH + INDEX, null, null);
        URLConnection connection = uri.toURL().openConnection();
        String result;
        try(Scanner scanner = new Scanner(connection.getInputStream())) {
            result = scanner.next();
        }
        return result;
    }
}
