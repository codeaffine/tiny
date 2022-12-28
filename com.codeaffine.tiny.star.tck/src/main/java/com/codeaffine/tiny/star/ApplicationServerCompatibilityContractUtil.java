package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.ApplicationServer.State;
import lombok.NoArgsConstructor;
import org.eclipse.swt.SWT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import static com.codeaffine.tiny.star.common.Threads.sleepFor;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class ApplicationServerCompatibilityContractUtil {

    static final String CLIENT_JS_PATH = format("rwt-resources/%s/rap-client.js", SWT.getVersion());
    static final String SCRIPT_REGISTRATION = format("<script type=\"text/javascript\" src=\"%s\"", CLIENT_JS_PATH);

    private static final long MILLIS_BETWEEN_RETRIES = 100L;
    private static final int RETRIES = 20;

    static final long MAX_RETRY_DURATION = RETRIES * MILLIS_BETWEEN_RETRIES / 1000;

    static void awaitState(ApplicationServer applicationServer, State expectedState) {
        awaitState(applicationServer, expectedState, RETRIES, MILLIS_BETWEEN_RETRIES);
    }

    static void awaitState(ApplicationServer applicationServer, State expectedState, int maxRetries, long millisBetweenRetries) {
        State state = applicationServer.getState();
        int retries = 0;
        while (expectedState != state && retries <= maxRetries) {
            retries++;
            sleepFor(millisBetweenRetries);
            state = applicationServer.getState();
        }
    }

    static String readContent(URL entryPointUrl) {
        try (Scanner scanner = new Scanner(entryPointUrl.openStream())) {
            return readContent(scanner);
        } catch (IOException cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    static String readContent(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream)) {
            return readContent(scanner);
        }
    }

    private static String readContent(Scanner scanner) {
        return scanner.useDelimiter("\\A").next();
    }
}
