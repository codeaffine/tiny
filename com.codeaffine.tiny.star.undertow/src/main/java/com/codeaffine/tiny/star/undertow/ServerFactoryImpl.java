/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import com.codeaffine.tiny.star.spi.ServerFactory;

import java.io.File;

public class ServerFactoryImpl implements ServerFactory {

    @Override
    public ServerImpl create(int port, String host, File workingDirectory, ApplicationConfiguration configuration) {
        return new ServerImpl(port, host, workingDirectory, configuration);
    }
}
