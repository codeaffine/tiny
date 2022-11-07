package com.codeaffine.tiny.star;

import static java.util.ServiceLoader.load;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerFactory;

import java.io.File;

class DelegatingServerFactory implements ServerFactory {

    @Override
    public Server create(int port, String host, File workingDirectory, ApplicationConfiguration configuration) {
        return load(ServerFactory.class)
            .iterator()
            .next()
            .create(port, host, workingDirectory, configuration);
    }
}
