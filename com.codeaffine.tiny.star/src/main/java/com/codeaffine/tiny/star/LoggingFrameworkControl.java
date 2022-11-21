package com.codeaffine.tiny.star;

public interface LoggingFrameworkControl {

    default void configure(ClassLoader applicationClassLoader, String applicationName) {}

    default void halt() {}

    default boolean isUsingWorkingDirectory() {
        return false;
    }
}
