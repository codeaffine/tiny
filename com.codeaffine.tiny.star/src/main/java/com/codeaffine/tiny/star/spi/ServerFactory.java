package com.codeaffine.tiny.star.spi;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.io.File;

public interface ServerFactory {

    Server create(int port, String host, File workingDirectory, ApplicationConfiguration configuration);

}
