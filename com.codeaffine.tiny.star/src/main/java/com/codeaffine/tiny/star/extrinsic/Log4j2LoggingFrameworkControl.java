package com.codeaffine.tiny.star.extrinsic;

import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.LoggingFrameworkControl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class Log4j2LoggingFrameworkControl implements LoggingFrameworkControl {

    @NonNull
    private final Log4j2ShutdownPreprocessor log4j2ShutdownPreprocessor;
    @NonNull
    private final Log4j2Configurator log4j2Configurator;

    Log4j2LoggingFrameworkControl() {
        this(new Log4j2ShutdownPreprocessor(), new Log4j2Configurator());
    }

    @Override
    public void configure(ClassLoader applicationClassLoader, String applicationName) {
        log4j2Configurator.run(applicationClassLoader, applicationName);
    }

    @Override
    public void halt() {
        log4j2ShutdownPreprocessor.run();
    }

    @Override
    public boolean isUsingWorkingDirectory() {
        return true;
    }
}
