package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.extrinsic.DelegatingLoggingFrameworkControl;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoggingFrameworkConfiguratorTest {

    @Test
    void configureLoggingFramework() {
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {}).build();
        LoggingFrameworkConfigurator configurator = new LoggingFrameworkConfigurator(applicationServer);

        LoggingFrameworkControl actual = configurator.configureLoggingFramework();

        assertThat(actual).isInstanceOf(DelegatingLoggingFrameworkControl.class);
    }

    @Test
    void configureLoggingFrameworkWithCustomImplementation() {
        LoggingFrameworkControl expected = mock(LoggingFrameworkControl.class);
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {})
            .withLoggingFrameworkControl(expected)
            .build();
        LoggingFrameworkConfigurator loggingFrameworkConfigurator = new LoggingFrameworkConfigurator(applicationServer);

        LoggingFrameworkControl actual = loggingFrameworkConfigurator.configureLoggingFramework();

        assertThat(actual).isSameAs(expected);
        verify(expected).configure(getClass().getClassLoader(), applicationServer.getIdentifier());
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new LoggingFrameworkConfigurator(null))
            .isInstanceOf(NullPointerException.class);
    }
}
