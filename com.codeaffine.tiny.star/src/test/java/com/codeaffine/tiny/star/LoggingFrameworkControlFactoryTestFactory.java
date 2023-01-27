package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;

public class LoggingFrameworkControlFactoryTestFactory implements LoggingFrameworkControlFactory {

    static class DummyLoggingFrameworkControl implements LoggingFrameworkControl {
    }

    @Override
    public LoggingFrameworkControl create() {
        return new DummyLoggingFrameworkControl();
    }
}
