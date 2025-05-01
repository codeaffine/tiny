/**
 * <p>Copyright (c) 2022-2025 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.test.fixtures;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.codeaffine.tiny.star.test.fixtures.UserSession.INITIAL_WIDGET_TREE_REQUEST_BODY;
import static com.codeaffine.tiny.star.test.fixtures.UserSession.newRwtPostRequest;
import static java.lang.System.getenv;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BOM test contract that checks that the application delivers an initial
 * start page and a widget tree having a "Hello World" label.
 */
public interface BomUiStartContract {

  @Test
  default void uiStart() throws Exception {
    String port = getenv().getOrDefault("PORT", "4711");
    String entryPointPath = getenv().getOrDefault("ENTRY_POINT_PATH", "/ui");
    URI uri = URI.create("http://localhost:" + port + entryPointPath);
    HttpResponse<String> startPage;
    HttpResponse<String> widgetTree;

    try (HttpClient httpClient = newHttpClient()){
      startPage = httpClient.send(
            HttpRequest.newBuilder().uri(uri).build(),
            BodyHandlers.ofString()
        );
        widgetTree = httpClient
          .send(
              newRwtPostRequest(INITIAL_WIDGET_TREE_REQUEST_BODY, uri),
              BodyHandlers.ofString()
          );
    }

    assertThat(startPage.statusCode()).isEqualTo(200);
    assertThat(startPage.body()).contains("<script type=\"text/javascript\" src=\"rwt-resources");
    assertThat(widgetTree.statusCode()).isEqualTo(200);
    assertThat(widgetTree.body()).contains("Hello World");
  }
}
