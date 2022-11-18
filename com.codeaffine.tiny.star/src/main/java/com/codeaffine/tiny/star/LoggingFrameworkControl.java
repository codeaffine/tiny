package com.codeaffine.tiny.star;

public interface LoggingFrameworkControl {

    interface LoggingFrameworkControlFactory {
        LoggingFrameworkControl create(ClassLoader applicationClassLoader);
    }

    void configure(ClassLoader applicationClassLoader, String applicationName);

    void halt();

    boolean isUsingWorkingDirectory();
}
