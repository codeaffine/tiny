/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.ApplicationServer.State;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.swt.SWT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import static com.codeaffine.tiny.shared.Threads.sleepFor;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class ApplicationServerCompatibilityContractUtil {

    private static final long MILLIS_BETWEEN_RETRIES = 100L;
    private static final int RETRIES = 20;

    static final String CLIENT_JS_PATH = format("rwt-resources/%s/rap-client.js", SWT.getVersion());
    static final String THEME_FALLBACK_JSON_PATH = "rwt-resources/rap-rwt.theme.Fallback.json";
    static final String THEME_DEFAULT_JSON_PATH = "rwt-resources/rap-rwt.theme.Default.json";
    static final String SCRIPT_REGISTRATION = format("<script type=\"text/javascript\" src=\"%s\"", CLIENT_JS_PATH);
    static final long MAX_RETRY_DURATION = RETRIES * MILLIS_BETWEEN_RETRIES / 1000;
    static final String PATH_SEPARATOR = "/";

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

    static String  readContent(URL entryPointUrl) {
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

    @SneakyThrows
    public static URL createResourceUrl(URL entryPointUrl, String resource) {
        return new URI(entryPointUrl.getProtocol(), null, entryPointUrl.getHost(), entryPointUrl.getPort(), PATH_SEPARATOR + resource, null, null).toURL();
    }
}
