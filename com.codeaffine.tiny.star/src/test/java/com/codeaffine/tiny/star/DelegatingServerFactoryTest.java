package com.codeaffine.tiny.star;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerFactory;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static com.codeaffine.tiny.star.ApplicationServerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.shared.ServiceLoaderAdapterTestHelper.fakeServiceLoaderAdapter;
import static com.codeaffine.tiny.star.Texts.ERROR_NO_SERVER_FACTORY_FOUND;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(ApplicationServerTestContext.class)
class DelegatingServerFactoryTest {

    private static final ApplicationConfiguration APPLICATION_CONFIGURATION = application -> {};
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    @TempDir
    private File workingDirectory;

    @Test
    void create() {
        ApplicationServer applicationServer = newApplicationServerBuilder(APPLICATION_CONFIGURATION)
            .withPort(PORT)
            .withHost(HOST)
            .build();
        DelegatingServerFactory factory = new DelegatingServerFactory(applicationServer);

        Server actual = factory.create(workingDirectory);

        assertThat(actual).isSameAs(CURRENT_SERVER.get());
        assertThat(CURRENT_SERVER.get().getPort()).isEqualTo(PORT);
        assertThat(CURRENT_SERVER.get().getHost()).isEqualTo(HOST);
        assertThat(CURRENT_SERVER.get().getWorkingDirectory()).isEqualTo(workingDirectory);
        assertThat(CURRENT_SERVER.get().getConfiguration()).isEqualTo(APPLICATION_CONFIGURATION);
    }

    @Test
    void createIfNoServerFactoryIsRegisteredOnClasspath() {
        ApplicationServer applicationServer = newApplicationServerBuilder(APPLICATION_CONFIGURATION).build();
        ServiceLoaderAdapter<ServerFactory> serviceLoaderAdapter = fakeServiceLoaderAdapter();
        DelegatingServerFactory factory = new DelegatingServerFactory(applicationServer, serviceLoaderAdapter);

        Exception actual = catchException(() -> factory.create(workingDirectory));

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(ERROR_NO_SERVER_FACTORY_FOUND);
    }

    @Test
    void createIfMoreThanOneServerFactoryIsRegisteredOnClasspath() {
        ApplicationServer applicationServer = newApplicationServerBuilder(APPLICATION_CONFIGURATION).build();
        ServerFactory factory1 = (port, host, workingDirectory, configuration) -> null;
        ServerFactory factory2 = (port, host, workingDirectory, configuration) -> null;
        ServiceLoaderAdapter<ServerFactory> serviceLoaderAdapter = fakeServiceLoaderAdapter(factory1, factory2);
        DelegatingServerFactory factory = new DelegatingServerFactory(applicationServer, serviceLoaderAdapter);

        Exception actual = catchException(() -> factory.create(workingDirectory));

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(factory1.getClass().getName())
            .hasMessageContaining(factory2.getClass().getName());
    }
    
    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new DelegatingServerFactory(null))
            .isInstanceOf(NullPointerException.class);
    }
}
