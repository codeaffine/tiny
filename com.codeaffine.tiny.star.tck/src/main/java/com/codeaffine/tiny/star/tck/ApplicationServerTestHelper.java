/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import com.codeaffine.tiny.star.spi.FilterDefinition;
import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import jakarta.servlet.Filter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Set;

import static com.codeaffine.tiny.star.ApplicationServer.DEFAULT_SESSION_TIMEOUT;
import static com.codeaffine.tiny.star.spi.FilterDefinition.of;
import static lombok.AccessLevel.PRIVATE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("java:S1075")
public class ApplicationServerTestHelper {

    public static final String ENTRYPOINT_PATH_1 = "/ep1";
    public static final String ENTRYPOINT_PATH_2 = "/ep2";
    public static final String HOST = "localhost";
    public static final int PORT = 1234;
    public static final ServerConfiguration CONFIGURATION = stubServerConfiguration(ENTRYPOINT_PATH_1);
    public static final ServerConfiguration MULTI_ENTRYPOINT_CONFIGURATION = stubServerConfiguration(ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2);
    public static final String FILTER_NAME_1 = "filter1";
    public static final String FILTER_NAME_2 = "filter2";
    public static final String FILTER_NAME_3 = "filter3";
    public static final FilterDefinition FILTER_DEFINITION_1 = of(FILTER_NAME_1, mock(Filter.class));
    public static final FilterDefinition FILTER_DEFINITION_2 = of(FILTER_NAME_2, mock(Filter.class), ENTRYPOINT_PATH_1);
    public static final FilterDefinition FILTER_DEFINITION_3 = of(FILTER_NAME_3, mock(Filter.class), ENTRYPOINT_PATH_1, ENTRYPOINT_PATH_2);
    public static final String KEY_STORE_PASSWORD = "store-password"; // common password for all test key-stores
    public static final String KEY_ALIAS = "tiny"; // common alias for test keystores that use an alias
    public static final String KEY_PASSWORD = "key-password"; // common password for test keystores that use a password that is different from the store password

    public static ServerConfiguration stubServerConfiguration(File workingDirectory, List<FilterDefinition> filterDefinitions, List<String> entryPointPaths) {
        ServerConfiguration result = stubServerConfiguration(workingDirectory, entryPointPaths.toArray(new String[0]));
        when(result.getFilterDefinitions()).thenReturn(filterDefinitions);
        return result;
    }

    public static ServerConfiguration stubServerConfiguration(File workingDirectory, String ... entryPointPaths) {
        ServerConfiguration result = stubServerConfiguration(entryPointPaths);
        when(result.getWorkingDirectory()).thenReturn(workingDirectory);
        return result;
    }

    public static ServerConfiguration stubServerConfiguration(String host, int port, SecureSocketLayerConfiguration secureSocketLayerConfiguration) {
        ServerConfiguration result = stubServerConfiguration(host, port);
        when(result.getSecureSocketLayerConfiguration()).thenReturn(secureSocketLayerConfiguration);
        return result;
    }

    public static ServerConfiguration stubServerConfiguration(String host, int port) {
        ServerConfiguration result = mock(ServerConfiguration.class);
        when(result.getHost()).thenReturn(host);
        when(result.getPort()).thenReturn(port);
        return result;
    }

    private static ServerConfiguration stubServerConfiguration(String ... entryPointPaths) {
        ServerConfiguration result = stubServerConfiguration(HOST, PORT);
        when(result.getEntryPointPaths()).thenReturn(Set.of(entryPointPaths));
        when(result.getContextClassLoader()).thenReturn(ApplicationServerTestHelper.class.getClassLoader());
        when(result.getContextListener()).thenReturn(mock(TinyStarServletContextListener.class));
        when(result.getSessionTimeout()).thenReturn(DEFAULT_SESSION_TIMEOUT);
        return result;
    }
}
