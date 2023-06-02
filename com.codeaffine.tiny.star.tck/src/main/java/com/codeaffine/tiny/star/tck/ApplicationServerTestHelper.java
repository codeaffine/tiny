/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import com.codeaffine.tiny.star.servlet.TinyStarServletContextListener;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Set;

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

    public static ServerConfiguration stubServerConfiguration(File workingDirectory, String ... entryPointPaths) {
        ServerConfiguration result = stubServerConfiguration(entryPointPaths);
        when(result.getWorkingDirectory()).thenReturn(workingDirectory);
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
        return result;
    }
}
