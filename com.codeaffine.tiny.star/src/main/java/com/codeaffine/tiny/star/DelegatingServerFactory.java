package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Texts.*;

import static java.lang.String.format;
import static java.util.ServiceLoader.load;
import static java.util.stream.Collectors.*;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import com.codeaffine.tiny.star.spi.Server;
import com.codeaffine.tiny.star.spi.ServerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor()
class DelegatingServerFactory implements ServerFactory {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final ServiceLoaderAdapter serviceLoaderAdapter;

    static class ServiceLoaderAdapter {

        List<ServerFactory> collectServerFactories() {
            List<ServerFactory> result = new ArrayList<>();
            for (ServerFactory serverFactory : load(ServerFactory.class)) {
                result.add(serverFactory);
            }
            return result;
        }
    }

    DelegatingServerFactory(ApplicationServer applicationServer) {
        this(applicationServer, new ServiceLoaderAdapter());
    }

    Server create(File workingDirectory) {
        return create(applicationServer.port, applicationServer.host, workingDirectory, applicationServer.applicationConfiguration);
    }

    @Override
    public Server create(int port, String host, File workingDirectory, ApplicationConfiguration configuration) {
        List<ServerFactory> serverFactories = serviceLoaderAdapter.collectServerFactories();
        if (serverFactories.isEmpty()) {
            throw new IllegalStateException(ERROR_NO_SERVER_FACTORY_FOUND);
        }
        if (serverFactories.size() > 1) {
            throw new IllegalStateException(format(ERROR_MORE_THAN_ONE_SERVER_FACTORY, collectFactoryClassNames(serverFactories)));
        }
        return serverFactories
            .get(0)
            .create(port, host, workingDirectory, configuration);
    }

    private static String collectFactoryClassNames(List<ServerFactory> serverFactories) {
        return serverFactories.stream()
            .map(factory -> factory.getClass().getName())
            .collect(joining(","));
    }
}
