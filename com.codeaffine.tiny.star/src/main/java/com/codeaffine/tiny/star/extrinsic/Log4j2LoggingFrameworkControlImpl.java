package com.codeaffine.tiny.star.extrinsic;

import com.codeaffine.tiny.star.LoggingFrameworkControl;

class Log4j2LoggingFrameworkControlImpl implements LoggingFrameworkControl {

    @Override
    public void configure(ClassLoader applicationClassLoader, String applicationName) {
        new Log4j2Configurator(applicationClassLoader, applicationName).run();
    }

    @Override
    public void halt() {
        new Log4j2ShutdownPreprocessor().run();
    }

    @Override
    public boolean isUsingWorkingDirectory() {
        return true;
    }
}
