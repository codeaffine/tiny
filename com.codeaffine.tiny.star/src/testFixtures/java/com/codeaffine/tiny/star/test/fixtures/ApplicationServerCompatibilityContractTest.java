/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.test.fixtures;

import com.codeaffine.tiny.star.ApplicationServer;

import java.io.File;
import java.net.URL;

import static com.codeaffine.tiny.shared.IoUtils.createTemporaryDirectory;
import static com.codeaffine.tiny.star.ApplicationServer.State;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static com.codeaffine.tiny.star.spi.FilterDefinition.of;
import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerCompatibilityContractUtil.*;
import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerContractContext.ENTRY_POINT_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * <p>Defines the contract test for {@link com.codeaffine.tiny.star.spi.Server} implementations to verify their support
 * of the {@link ApplicationServer} API.</p>
 *
 * <p>JUnit test implementations of this contract are expected to extend this interface and implement the {@link #useSecureSocketLayerConfiguration()}
 * method. To support both HTTP and HTTPS, two test classes are required, one for each scheme.</p>
 */
@ApplicationServerCompatibilityTest
@SuppressWarnings("java:S5960")
public interface ApplicationServerCompatibilityContractTest {

    /**
     * Defines whether the application server under test will use a secure socket layer for communication.
     *
     * @return {@code true} if the application server under test will use a secure socket layer for communication, {@code false} otherwise.
     */
    boolean useSecureSocketLayerConfiguration();

    @StartApplicationServer
    default void startApplicationServer(ApplicationServerContractContext context) {
        File temporalDirectory = createTemporaryDirectory(getClass().getName());
        temporalDirectory.deleteOnExit();
        context.setWorkingDirectory(temporalDirectory);
        ApplicationServer applicationServer = newApplicationServerBuilder(context::configure)
            .withLifecycleListener(context)
            .withWorkingDirectory(context.getWorkingDirectory())
            .withFilterDefinition(of(context, ENTRY_POINT_PATH))
            .withSecureSocketLayerConfiguration(useSecureSocketLayerConfiguration() ? context.getSecureSocketLayerConfiguration() : null)
            .build();

        State initialState = applicationServer.getState();
        applicationServer.start();
        awaitState(applicationServer, RUNNING);
        State actual = applicationServer.getState();

        assertThat(initialState) // NOSONAR
            .describedAs("Application server was expected to be %s but was %s.", HALTED, initialState)
            .isSameAs(HALTED);
        assertThat(actual)
            .describedAs("Application server did not start within %s seconds.", MAX_RETRY_DURATION)
            .isSameAs(RUNNING);
    }

    @StopApplicationServer
    default void stopApplicationServer(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();

        State initialState = applicationServer.getState();
        applicationServer.stop();
        awaitState(applicationServer, HALTED);
        State afterStop = applicationServer.getState();
        boolean successRecreatingWorkingDirectory = context.getWorkingDirectory().mkdir();
        applicationServer.start();
        awaitState(applicationServer, RUNNING);
        State afterRestart = applicationServer.getState();
        applicationServer.stop();
        awaitState(applicationServer, HALTED);
        State afterFinalStop = applicationServer.getState();

        assertThat(initialState)
            .describedAs("Application server was expected to be %s but was %s.", RUNNING, initialState)
            .isSameAs(RUNNING);
        assertThat(afterStop)
            .describedAs("Application server did not stop within %s seconds.", MAX_RETRY_DURATION)
            .isSameAs(HALTED);
        assertThat(afterRestart)
            .describedAs("Application server was expected to be %s after restart but was %s.", RUNNING, initialState)
            .isSameAs(RUNNING);
        assertThat(afterFinalStop)
            .describedAs("Application server did not stop finally within %s seconds.", MAX_RETRY_DURATION)
            .isSameAs(HALTED);
        assertThat(successRecreatingWorkingDirectory)
            .describedAs("Could not recreate working directory %s.", context.getWorkingDirectory())
            .isTrue();
        assertThat(context.getWorkingDirectory())
            .describedAs("Working directory %s still exists.", context.getWorkingDirectory())
            .doesNotExist();
        assertThat(context.isFilterInitialized()).isTrue();
        assertThat(context.isFilterDestroyed()).isTrue();
        assertThat(context.isDoFilterCalled()).isTrue();
    }

    @RequestApplicationServer
    default void simulateUserSession(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();

        UserSession actual = context.simulateUserSession(applicationServer.getUrls()[0]);
        actual.simulateSessionInteraction();

        assertThat(actual.getStartupPageResponseBody())
            .contains(SCRIPT_REGISTRATION);
        assertThat(actual.getWidgetTreeInitializationResponseBody())
            .contains(TestEntryPoint.BUTTON_LABEL + actual.getTrackingId());
        assertThat(actual.getButtonPushedResponseBody())
            .contains(TestEntryPoint.BUTTON_AFTER_PUSHED_LABEL + actual.getTrackingId());
        context.verifyServerTrustCheckInvocation(actual);
    }

    @RequestApplicationServer
    default void simulateMultipleUserSessions(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();
        URL url = applicationServer.getUrls()[0];

        UserSession actual1 = context.simulateUserSession(url);
        actual1.simulateSessionInteraction();
        UserSession actual2 = context.simulateUserSession(url);
        actual2.simulateSessionInteraction();

        assertThat(actual1.getButtonPushedResponseBody())
            .contains(TestEntryPoint.BUTTON_AFTER_PUSHED_LABEL + actual1.getTrackingId());
        assertThat(actual2.getButtonPushedResponseBody())
            .contains(TestEntryPoint.BUTTON_AFTER_PUSHED_LABEL + actual2.getTrackingId());
        assertThat(actual1.getTrackingId()).isNotEqualTo(actual2.getTrackingId());
        context.verifyServerTrustCheckInvocation(actual1, actual2);
    }

    @RequestApplicationServer
    default void requestStaticResources(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();
        URL url = applicationServer.getUrls()[0];
        Runnable serverTrustedCheckObserverClientJsPath = mock(Runnable.class);
        Runnable serverTrustedCheckObserverThemeFallbackJsPath = mock(Runnable.class);
        Runnable serverTrustedCheckObserverThemeDefaultJsPath = mock(Runnable.class);

        String actualClientScript = readContent(createResourceUrl(url, CLIENT_JS_PATH), serverTrustedCheckObserverClientJsPath);
        String actualThemeFallbackJson = readContent(createResourceUrl(url, THEME_FALLBACK_JSON_PATH), serverTrustedCheckObserverThemeFallbackJsPath);
        String actualThemeDefaultJson = readContent(createResourceUrl(url, THEME_DEFAULT_JSON_PATH), serverTrustedCheckObserverThemeDefaultJsPath);

        ClassLoader classLoader = getClass().getClassLoader();
        assertThat(actualClientScript).contains(readContent(classLoader.getResourceAsStream("client.js")));
        assertThat(actualThemeFallbackJson).isNotNull();
        assertThat(actualThemeDefaultJson).isNotNull();
        context.verifyServerTrustCheckInvocation(
            serverTrustedCheckObserverClientJsPath,
            serverTrustedCheckObserverThemeFallbackJsPath,
            serverTrustedCheckObserverThemeDefaultJsPath
        );
    }
}
