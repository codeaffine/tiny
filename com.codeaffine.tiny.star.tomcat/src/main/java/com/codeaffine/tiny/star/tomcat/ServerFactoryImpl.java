package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.ServerFactory;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.io.File;

public class ServerFactoryImpl implements ServerFactory {

    @Override
    public ServerImpl create(int port, String host, File workingDirectory, ApplicationConfiguration configuration) {
        return new ServerImpl(port, host, workingDirectory, configuration);
    }
}
