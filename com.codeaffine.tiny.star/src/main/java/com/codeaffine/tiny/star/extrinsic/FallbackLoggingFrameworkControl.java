package com.codeaffine.tiny.star.extrinsic;

import com.codeaffine.tiny.star.LoggingFrameworkControl;

class FallbackLoggingFrameworkControl implements LoggingFrameworkControl {

    @Override
    public void configure(ClassLoader applicationClassLoader, String applicationName) {
        // do nothing
    }

    @Override
    public void halt() {
        // do nothing
    }

    @Override
    public boolean isUsingWorkingDirectory() {
        return false;
    }
}
