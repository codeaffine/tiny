package com.codeaffine.tiny.star.extrinsic;

import static java.util.Map.*;

import com.codeaffine.tiny.star.LoggingFrameworkControl;

import java.util.Map;
import lombok.NonNull;
import lombok.experimental.Delegate;

class DelegatingLoggingFrameworkControl implements LoggingFrameworkControl {

    private static final Map<String, LoggingFrameworkControl> SUPPORTED_LOGGING_FRAMEWORKS = of(
        "org.apache.logging.slf4j.SLF4JServiceProvider", new Log4j2LoggingFrameworkControlImpl()
    );
    
    @Delegate
    private final LoggingFrameworkControl delegate;

    DelegatingLoggingFrameworkControl(@NonNull ClassLoader applicationClassLoader) {
        this.delegate = findMatchingLoggingFrameworkControl(applicationClassLoader, new FallbackLoggingFrameworkControl());
    }

    private LoggingFrameworkControl findMatchingLoggingFrameworkControl(
        ClassLoader applicationClassLoader,
        LoggingFrameworkControl fallbackLoggingFrameworkControl)
    {
        return SUPPORTED_LOGGING_FRAMEWORKS.entrySet()
            .stream()
            .filter(entry -> isLoggingFrameworkAvailable(applicationClassLoader, entry.getKey()))
            .map(Entry::getValue)
            .findFirst()
            .orElse(fallbackLoggingFrameworkControl);
    }

    private static boolean isLoggingFrameworkAvailable(ClassLoader applicationClassLoader, String key) {
        try {
            applicationClassLoader.loadClass(key);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
