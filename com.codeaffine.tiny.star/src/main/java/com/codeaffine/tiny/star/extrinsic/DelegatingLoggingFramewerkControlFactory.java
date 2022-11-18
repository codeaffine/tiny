package com.codeaffine.tiny.star.extrinsic;

import static com.codeaffine.tiny.star.LoggingFrameworkControl.*;

import com.codeaffine.tiny.star.LoggingFrameworkControl;

public class DelegatingLoggingFramewerkControlFactory implements LoggingFrameworkControlFactory {

    @Override
    public LoggingFrameworkControl create(ClassLoader applicationClassLoader) {
        return new DelegatingLoggingFrameworkControl(applicationClassLoader);
    }
}
