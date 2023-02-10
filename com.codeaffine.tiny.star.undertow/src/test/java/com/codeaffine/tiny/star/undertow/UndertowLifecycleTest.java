package com.codeaffine.tiny.star.undertow;

import static com.codeaffine.tiny.shared.IoUtils.findFreePort;
import static com.codeaffine.tiny.star.Protocol.HTTP;
import static com.codeaffine.tiny.star.undertow.DeploymentOperation.CONTEXT_PATH;
import static com.codeaffine.tiny.star.undertow.HttpHandlerStarter.PREFIX_PATH;
import static io.undertow.Handlers.path;
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;

import static java.lang.String.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static java.nio.file.Files.writeString;
import static java.util.Objects.nonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import java.io.File;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import lombok.SneakyThrows;

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
        URL url = new URL(HTTP.name().toLowerCase(), HOST, port, CONTEXT_PATH + INDEX);
        URLConnection connection = url.openConnection();
        String result;
        try(Scanner scanner = new Scanner(connection.getInputStream())) {
            result = scanner.next();
        }
        return result;
    }
}
