/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers;
import static java.nio.file.Files.isDirectory;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

    @Test
    void workDirStructure() {
        Path workDir = Paths.get("build/app-workdir");

        assertThat(isDirectory(workDir)).isTrue();
        assertThat(workDir.resolve("logs").resolve("application.log")).isRegularFile();
        assertThat(workDir.resolve("rwt-resources")).isNotEmptyDirectory();
    }

    @Test
    void uiStart() throws IOException, InterruptedException {
        String port = getenv().getOrDefault("PORT", "4711");
        String entryPointPath = getenv().getOrDefault("ENTRY_POINT_PATH", "/ui");
        String url = format("http://localhost:%s%s", port, entryPointPath);
        HttpResponse<String> startPage = requestStartPage(url);
        HttpResponse<String> widgetTree = requestWidgetTree(url);

        assertThat(startPage.statusCode()).isEqualTo(200);
        assertThat(startPage.body()).contains("<script type=\"text/javascript\" src=\"rwt-resources");
        assertThat(widgetTree.statusCode()).isEqualTo(200);
        assertThat(widgetTree.body()).contains("Hello World");
    }

    private static HttpResponse<String> requestStartPage(String url) throws IOException, InterruptedException {
        try(HttpClient client = newHttpClient()) {
            return client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build(),
                BodyHandlers.ofString()
            );
        }
    }

    private static HttpResponse<String> requestWidgetTree(String url) throws IOException, InterruptedException {
        try(HttpClient client = newHttpClient()) {
            return client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .headers(
                        "Accept", "*/*",
                        "Accept-Encoding", "gzip, deflate, br",
                        "Accept-Language", "en-US,en;q=0.9,de;q=0.8,yo;q=0.7",
                        "Content-Type", "application/json; charset=UTF-8")
                    .POST(ofString("""
                    {
                      "head":{"requestCounter":0},
                      "operations":[
                        ["set","w1",{"dpi":[96,96],"colorDepth":24}],
                        ["set","rwt.client.ClientInfo",{"timezoneOffset":-60}],
                        ["set","w1",{"cursorLocation":[0,0],"bounds":[0,0,982,792]}]
                      ]
                    }
                    """))
                    .build(),
                BodyHandlers.ofString());
        }
    }
}
