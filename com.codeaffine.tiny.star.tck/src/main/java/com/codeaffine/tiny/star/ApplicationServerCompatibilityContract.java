package com.codeaffine.tiny.star;

import org.eclipse.rap.rwt.application.Application;

import java.io.File;
import java.io.IOException;
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
        File workingDirectory = createTemporayDirectory(getClass().getName());
        ApplicationServer applicationServer = newApplicationServerBuilder(ApplicationServerCompatibilityContract::configure)
            .withLifecycleListener(context)
            .withWorkingDirectory(workingDirectory)
            .build();

        State initialState = applicationServer.getState();
        applicationServer.start();
        awaitState(applicationServer, RUNNING);
        State actual = applicationServer.getState();

        assertThat(initialState) // NOSONAR
            .describedAs("Application server was expected to be %s but was %s.", HALTED, initialState)
            .isSameAs(HALTED);
        assertThat(actual)
            .describedAs("Application server did not start within %s seconds", MAX_RETRY_DURATION)
            .isSameAs(RUNNING);
    }

    @StopApplicationServer
    default void stopApplicationServer(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();

        State initialState = applicationServer.getState();
        applicationServer.stop();
        awaitState(applicationServer, HALTED);
        State actual = applicationServer.getState();

        assertThat(initialState)
            .describedAs("Application server was expected to be %s but was %s.", RUNNING, initialState)
            .isSameAs(RUNNING);
        assertThat(actual)
            .describedAs("Application server did not stop within %s seconds", MAX_RETRY_DURATION)
            .isSameAs(HALTED);
    }

    @RequestApplicationServer
    default void requestEntryPoint(ApplicationServerContractContext context) {
        ApplicationServer applicationServer = context.getApplicationServer();
        URL entryPointUrl = applicationServer.getUrls()[0];

        String actual = readContent(entryPointUrl);

        assertThat(actual).contains(SCRIPT_REGISTRATION);
    }

    @RequestApplicationServer
    default void requestClientScript(ApplicationServerContractContext context) throws IOException {
        ApplicationServer applicationServer = context.getApplicationServer();
        URL entryPointUrl = applicationServer.getUrls()[0];
        URL clientScriptUrl = new URL(entryPointUrl.getProtocol(), entryPointUrl.getHost(), entryPointUrl.getPort(), "/" + CLIENT_JS_PATH);

        String actual = readContent(clientScriptUrl);

        assertThat(actual).contains(readContent(getClass().getClassLoader().getResourceAsStream("client.js")));
    }

    private static void configure(Application application) {
        application.addEntryPoint("/ui", TestEntryPoint.class, null);
    }
}
