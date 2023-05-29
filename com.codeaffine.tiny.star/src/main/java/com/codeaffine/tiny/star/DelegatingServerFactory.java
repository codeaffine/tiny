/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Texts.*;

import static java.lang.String.format;
import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerFactory;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class DelegatingServerFactory implements ServerFactory {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final ServiceLoaderAdapter<ServerFactory> serviceLoaderAdapter;

    DelegatingServerFactory(ApplicationServer applicationServer) {
        this(applicationServer, new ServiceLoaderAdapter<>(ServerFactory.class, ServiceLoader::load));
    }

    Server create(File workingDirectory) {
        return create(applicationServer.port, applicationServer.host, workingDirectory, applicationServer.applicationConfiguration);
    }

    @Override
    public Server create(int port, String host, File workingDirectory, ApplicationConfiguration configuration) {
        List<ServerFactory> serverFactories = serviceLoaderAdapter.collectServiceTypeFactories();
        if (serverFactories.isEmpty()) {
            throw new IllegalStateException(ERROR_NO_SERVER_FACTORY_FOUND);
        }
        if (serverFactories.size() > 1) {
            throw new IllegalStateException(format(ERROR_MORE_THAN_ONE_SERVER_FACTORY, serviceLoaderAdapter.collectServiceTypeFactoryClassNames()));
        }
        return serverFactories
            .get(0)
            .create(port, host, workingDirectory, configuration);
    }
}
