/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import com.codeaffine.tiny.star.ApplicationServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;
import java.net.URL;

import static com.codeaffine.tiny.star.ApplicationServer.State;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationServer.State.STOPPING;
import static java.io.File.createTempFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationServerCompatibilityContractUtilTest {

    private static final String RESOURCE_PATH = "resource/path";

    @ParameterizedTest(name = "[{index}] awaiting state {2} with server having initial state {0} and retry state {1} expects server to provide"
        + " state {3} after call.")
    @CsvSource({
        "RUNNING, STOPPING, RUNNING, STOPPING",
        "HALTED, RUNNING, RUNNING, STOPPING",
        "HALTED, STARTING, RUNNING, HALTED",
    })
    void awaitState(State initialState, State stateAfterOneRetry, State toWaitFor, State expectedState) {
        ApplicationServer applicationServer = mock(ApplicationServer.class);
        when(applicationServer.getState()).thenReturn(initialState, stateAfterOneRetry, STOPPING, HALTED);

        ApplicationServerCompatibilityContractUtil.awaitState(applicationServer, toWaitFor, 1, 40L);
        State actual = applicationServer.getState();

        assertThat(actual).isSameAs(expectedState);
    }

    @Test
    void readContentFromUrl() {
        String expected = "Hello\nWorld!";
        URL url = createUrlToContent(expected);

        String actual = ApplicationServerCompatibilityContractUtil.readContent(url);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void readContentFromUrlWithIoProblem() throws IOException {
        URL url = new File("doesNotExist").toURI().toURL();

        Exception actual = catchException(() -> ApplicationServerCompatibilityContractUtil.readContent(url));

        assertThat(actual).isInstanceOf(IllegalArgumentException.class)
            .hasCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    void readContentFromInputStream() {
        String expected = "Hello\nWorld!";
        InputStream inputStream = new ByteArrayInputStream(expected.getBytes(UTF_8));

        String actual = ApplicationServerCompatibilityContractUtil.readContent(inputStream);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createResourceUrl() {
        URL url = createUrlToContent("content");

        URL actual = ApplicationServerCompatibilityContractUtil.createResourceUrl(url, RESOURCE_PATH);

        assertThat(actual)
            .hasProtocol(url.getProtocol())
            .hasHost(url.getHost())
            .hasPort(url.getPort())
            .hasPath(ApplicationServerCompatibilityContractUtil.PATH_SEPARATOR + RESOURCE_PATH);
    }

    @SneakyThrows
    private static URL createUrlToContent(String content) {
        File tmpFile = createTempFile("readContentTest", ".tmp");
        tmpFile.deleteOnExit();
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(content);
        writer.close();
        return tmpFile.toURI().toURL();
    }
}
