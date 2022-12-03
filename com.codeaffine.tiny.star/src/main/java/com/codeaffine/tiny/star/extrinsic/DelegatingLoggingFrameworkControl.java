package com.codeaffine.tiny.star.extrinsic;

import static com.codeaffine.tiny.star.extrinsic.Texts.ERROR_MORE_THAN_ONE_SLF4J_SERVICE_PROVIDER_DETECTED;

import static java.lang.String.format;
import static java.util.Map.Entry;
import static java.util.Map.of;
import static java.util.stream.Collectors.toMap;

import com.codeaffine.tiny.star.LoggingFrameworkControl;

import java.util.Map;
import lombok.NonNull;
import lombok.experimental.Delegate;

public class DelegatingLoggingFrameworkControl implements LoggingFrameworkControl {

    static final Map<String, LoggingFrameworkControl> SUPPORTED_LOGGING_FRAMEWORKS = of(
        "org.apache.logging.slf4j.SLF4JServiceProvider", new Log4j2LoggingFrameworkControl()
    );

    @Delegate
    private final LoggingFrameworkControl delegate;

    public DelegatingLoggingFrameworkControl(ClassLoader applicationClassLoader) {
        this(applicationClassLoader, SUPPORTED_LOGGING_FRAMEWORKS, new LoggingFrameworkControl() {});
    }

    DelegatingLoggingFrameworkControl(
        @NonNull ClassLoader applicationClassLoader,
        @NonNull Map<String, LoggingFrameworkControl> supportedLoggingFrameworks,
        @NonNull LoggingFrameworkControl fallbackLoggingFrameworkControl)
    {
        this.delegate = findMatchingLoggingFrameworkControl(applicationClassLoader, supportedLoggingFrameworks, fallbackLoggingFrameworkControl);
    }

    private static LoggingFrameworkControl findMatchingLoggingFrameworkControl(
        ClassLoader applicationClassLoader,
        Map<String, LoggingFrameworkControl> supportedLoggingFrameworks,
        LoggingFrameworkControl fallbackLoggingFrameworkControl)
    {
        Map<String, LoggingFrameworkControl> activeFrameworks = supportedLoggingFrameworks.entrySet()
            .stream()
            .filter(entry -> isLoggingFrameworkAvailable(applicationClassLoader, entry.getKey()))
            .collect(toMap(Entry::getKey, Entry::getValue));
        if(activeFrameworks.size() > 1) {
            throw new IllegalStateException(format(ERROR_MORE_THAN_ONE_SLF4J_SERVICE_PROVIDER_DETECTED, activeFrameworks.keySet()));
        }
        if(activeFrameworks.isEmpty()) {
            return fallbackLoggingFrameworkControl;
        }
        return activeFrameworks.values()
            .iterator()
            .next();
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
