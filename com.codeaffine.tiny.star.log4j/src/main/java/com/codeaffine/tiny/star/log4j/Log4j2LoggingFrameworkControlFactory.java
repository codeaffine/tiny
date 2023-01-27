package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;

public class Log4j2LoggingFrameworkControlFactory implements LoggingFrameworkControlFactory {

    @Override
    public LoggingFrameworkControl create() {
        return new Log4j2LoggingFrameworkControl();
    }
}
