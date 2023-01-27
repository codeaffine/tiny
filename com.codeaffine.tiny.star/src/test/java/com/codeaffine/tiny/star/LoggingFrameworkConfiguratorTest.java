package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LoggingFrameworkConfiguratorTest {

    @Test
    void configureLoggingFramework() {
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {}).build();
        LoggingFrameworkConfigurator configurator = new LoggingFrameworkConfigurator(applicationServer);

        LoggingFrameworkControl actual = configurator.configureLoggingFramework();

        assertThat(actual).isInstanceOf(LoggingFrameworkControlFactoryTestFactory.DummyLoggingFrameworkControl.class);
    }

    @Test
    void configureLoggingFrameworkWithCustomImplementation() {
        LoggingFrameworkControl expected = mock(LoggingFrameworkControl.class);
        LoggingFrameworkControlFactory loggingFrameworkControlFactory = stubLoggingFrameworkControlFactory(expected);
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {}).build();
        LoggingFrameworkConfigurator loggingFrameworkConfigurator = new LoggingFrameworkConfigurator(applicationServer, loggingFrameworkControlFactory);

        LoggingFrameworkControl actual = loggingFrameworkConfigurator.configureLoggingFramework();

        assertThat(actual).isSameAs(expected);
        verify(expected).configure(getClass().getClassLoader(), applicationServer.getIdentifier());
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new LoggingFrameworkConfigurator(null))
            .isInstanceOf(NullPointerException.class);
    }

    private static LoggingFrameworkControlFactory stubLoggingFrameworkControlFactory(LoggingFrameworkControl expected) {
        LoggingFrameworkControlFactory result = mock(LoggingFrameworkControlFactory.class);
        when(result.create()).thenReturn(expected);
        return result;
    }
}
