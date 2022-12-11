package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PACKAGE;

import static java.util.Objects.isNull;

import com.codeaffine.tiny.star.extrinsic.DelegatingLoggingFrameworkControl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class LoggingFrameworkConfigurator {

    @NonNull
    private final ApplicationServer applicationServer;

    LoggingFrameworkControl configureLoggingFramework() {
        ClassLoader applicationClassLoader = applicationServer.applicationConfiguration.getClass().getClassLoader();
        LoggingFrameworkControl result = getLoggingFrameworkControl(applicationClassLoader);
        result.configure(applicationClassLoader, applicationServer.getIdentifier());
        return result;
    }

    private LoggingFrameworkControl getLoggingFrameworkControl(ClassLoader applicationClassLoader) {
        if (isNull(applicationServer.loggingFrameworkControl)) {
            return new DelegatingLoggingFrameworkControl(applicationClassLoader);
        }
        return applicationServer.loggingFrameworkControl;
    }
}
