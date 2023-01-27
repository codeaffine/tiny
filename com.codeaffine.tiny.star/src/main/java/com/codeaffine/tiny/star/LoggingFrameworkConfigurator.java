package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class LoggingFrameworkConfigurator {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final LoggingFrameworkControlFactory loggingFrameworkControlFactory;

    LoggingFrameworkConfigurator(ApplicationServer applicationServer) {
        this(applicationServer, new DelegatingLoggingFrameworkControlFactory());
    }

    LoggingFrameworkControl configureLoggingFramework() {
        ClassLoader applicationClassLoader = applicationServer.applicationConfiguration.getClass().getClassLoader();
        LoggingFrameworkControl result = loggingFrameworkControlFactory.create();
        result.configure(applicationClassLoader, applicationServer.getIdentifier());
        return result;
    }
}
