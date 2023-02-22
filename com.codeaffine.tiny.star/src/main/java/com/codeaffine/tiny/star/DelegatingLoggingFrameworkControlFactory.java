package com.codeaffine.tiny.star;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ServiceLoader;

import static com.codeaffine.tiny.star.Texts.ERROR_MORE_THAN_ONE_LOGGING_FRAMEWORK_CONTROL_FACTORY;
import static java.lang.String.format;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class DelegatingLoggingFrameworkControlFactory implements LoggingFrameworkControlFactory {

    @NonNull
    private final ServiceLoaderAdapter<LoggingFrameworkControlFactory> serviceLoaderAdapter;

    static class DummyLoggingFrameworkControl implements LoggingFrameworkControl {}

    DelegatingLoggingFrameworkControlFactory() {
        this(new ServiceLoaderAdapter<>(LoggingFrameworkControlFactory.class, ServiceLoader::load));
    }

    @Override
    public LoggingFrameworkControl create() {
        List<LoggingFrameworkControlFactory> factories = serviceLoaderAdapter.collectServiceTypeFactories();
        if (factories.isEmpty()) {
            factories.add(DummyLoggingFrameworkControl::new);
        }
        if (factories.size() > 1) {
            String message = format(ERROR_MORE_THAN_ONE_LOGGING_FRAMEWORK_CONTROL_FACTORY, serviceLoaderAdapter.collectServiceTypeFactoryClassNames());
            throw new IllegalStateException(message);
        }
        return factories
            .get(0)
            .create();
    }
}
