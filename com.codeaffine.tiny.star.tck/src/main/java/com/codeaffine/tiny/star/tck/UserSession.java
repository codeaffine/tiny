package com.codeaffine.tiny.star.tck;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.rap.rwt.application.EntryPointFactory;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
class UserSession {

    private static final String INITIAL_WIDGET_TREE_REQUEST_BODY = """
        {
          "head":{"requestCounter":0},
          "operations":[
            ["set","w1",{"dpi":[96,96],"colorDepth":24}],
            ["set","rwt.client.ClientInfo",{"timezoneOffset":-60}],
            ["set","w1",{"cursorLocation":[0,0],"bounds":[0,0,982,792]}]
          ]
        }
        """;
    private static final String BUTTON_PUSHED_REQUEST_BODY = """
        {
          "head":{"requestCounter":1},
          "operations":[
            ["set","w2",{"mode":"maximized","bounds":[0,0,647,792],"activeControl":"w3"}],
            ["notify","w3","Selection",{"button":1,"shiftKey":false,"ctrlKey":false,"altKey":false}],
            ["set","w1",{"cursorLocation":[442,519],"focusControl":"w3"}]
          ]
        }
        """;

    @NonNull
    private final URL url;
    @NonNull
    private final AtomicReference<EntryPointFactory> entryPointFactoryHub;
    @NonNull
    @Getter
    private final String trackingId;

    @Getter
    private String startupPageResponseBody;
    @Getter
    private String widgetTreeInitializationResponseBody;
    @Getter
    private String buttonPushedResponseBody;
    private HttpClient httpClient;

    void simulateSessionInteraction() {
        entryPointFactoryHub.set(() -> new TestEntryPoint(trackingId));
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpRequest initialRequest = newHttpGetRequest();
        HttpResponse<String> initialResponse = sendRequest(initialRequest, cookieManager);
        startupPageResponseBody = initialResponse.body();
        HttpRequest widgetTreeInitializationRequest = newRwtPostRequest(INITIAL_WIDGET_TREE_REQUEST_BODY, urlToUri());
        HttpResponse<String> widgetTreeInitializationResponse = sendRequest(widgetTreeInitializationRequest, cookieManager);
        widgetTreeInitializationResponseBody = widgetTreeInitializationResponse.body();
        HttpRequest buttonPushedRequest = newRwtPostRequest(BUTTON_PUSHED_REQUEST_BODY, urlToUriWithCidParameter(parseCid()));
        HttpResponse<String> buttonPushedResponse = sendRequest(buttonPushedRequest, cookieManager);
        buttonPushedResponseBody = buttonPushedResponse.body();
    }

    private HttpRequest newHttpGetRequest() {
        return HttpRequest.newBuilder()
            .uri(urlToUri())
            .GET()
            .build();
    }

    private static HttpRequest newRwtPostRequest(String rwtPostRequestBody, URI uri) {
        return HttpRequest.newBuilder()
            .uri(uri)
            .headers(
                "Accept", "*/*",
                "Accept-Encoding", "gzip, deflate, br",
                "Accept-Language", "en-US,en;q=0.9,de;q=0.8,yo;q=0.7",
                "Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(rwtPostRequestBody))
            .build();
    }

    @SneakyThrows(IOException.class)
    private HttpResponse<String> sendRequest(HttpRequest request, CookieManager cookieManager) {
        try {
            return ensureHttpClientExists(cookieManager)
                .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(cause);
        }
    }

    private HttpClient ensureHttpClientExists(CookieManager cookieManager) {
        if (isNull(httpClient)) {
            httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
        }
        return httpClient;
    }

    @SneakyThrows({URISyntaxException.class})
    private URI urlToUri() {
        return url.toURI();
    }

    @SneakyThrows({URISyntaxException.class, MalformedURLException.class})
    private URI urlToUriWithCidParameter(String cid) {
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile() + "?cid=" + cid)
            .toURI();
    }

    private String parseCid() {
        return widgetTreeInitializationResponseBody.split(",")[0]
            .split(":")[2]
            .replace("\"", "")
            .replace("}", "");
    }
}
