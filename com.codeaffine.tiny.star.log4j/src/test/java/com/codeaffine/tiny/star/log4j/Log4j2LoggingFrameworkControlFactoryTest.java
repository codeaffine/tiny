package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class Log4j2LoggingFrameworkControlFactoryTest {

    @Test
    void load() {
        ServiceLoaderAdapter<LoggingFrameworkControlFactory> loader = new ServiceLoaderAdapter<>(LoggingFrameworkControlFactory.class, ServiceLoader::load);

        List<LoggingFrameworkControlFactory> actual = loader.collectServiceTypeFactories();

        assertThat(actual)
            .hasSize(1)
            .allSatisfy(factory -> assertThat(factory.getClass()).isSameAs(Log4j2LoggingFrameworkControlFactory.class))
            .allSatisfy(factory -> assertThat(factory.create()).isInstanceOf(Log4j2LoggingFrameworkControl.class));
    }
}
