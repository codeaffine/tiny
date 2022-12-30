package com.codeaffine.tiny.star;

import java.net.URL;

import static com.codeaffine.tiny.star.ApplicationServer.State;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static com.codeaffine.tiny.star.ApplicationServerCompatibilityContractUtil.*;
import static com.codeaffine.tiny.star.common.IoUtils.createTemporayDirectory;
import static org.assertj.core.api.Assertions.assertThat;

@ApplicationServerCompatibilityTest
@SuppressWarnings("java:S5960")
public interface ApplicationServerCompatibilityContract {

    @StartApplicationServer
    default void startApplicationServer(ApplicationServerContractContext context) {
        context.setWorkingDirectory(createTemporayDirectory(getClass().getName()));
        ApplicationServer applicationServer = newApplicationServerBuilder(context::configure)
            .withLifecycleListener(context)
            .withWorkingDirectory(context.getWorkingDirectory())
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
    }

    @RequestApplicationServer
    default void requestStaticResources(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();
        URL url = applicationServer.getUrls()[0];

        String actualClientScript = readContent(createResourceUrl(url, CLIENT_JS_PATH));
        String actualThemeFallbackJson = readContent(createResourceUrl(url, THEME_FALLBACK_JSON_PATH));
        String actualThemeDefaultJson = readContent(createResourceUrl(url, THEME_DEFAULT_JSON_PATH));

        ClassLoader classLoader = getClass().getClassLoader();
        assertThat(actualClientScript).contains(readContent(classLoader.getResourceAsStream("client.js")));
        assertThat(actualThemeFallbackJson).isNotNull();
        assertThat(actualThemeDefaultJson).isNotNull();
    }
}
