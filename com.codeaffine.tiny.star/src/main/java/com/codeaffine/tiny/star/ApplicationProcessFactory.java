package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ShutdownHookHandler.beforeProcessShutdown;
import static com.codeaffine.tiny.star.Texts.INFO_SERVER_USAGE;
import static com.codeaffine.tiny.star.Texts.INFO_WORKING_DIRECTORY;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class ApplicationProcessFactory {

    static final @NonNull BiConsumer<ApplicationServer, ApplicationProcess> LIFECYCLE_LISTENER_REGISTRAR
        = (appServer, appProcess) -> appServer.lifecycleListeners.forEach(appProcess::registerLifecycleListener);

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    private final WorkingDirectoryPreparer workingDirectoryPreparer;
    @NonNull
    private final LoggingFrameworkConfigurator loggingFrameworkConfigurator;
    @NonNull
    private final DelegatingServerFactory delegatingServerFactory;
    @NonNull
    private final TerminatorFactory terminatorFactory;
    @NonNull
    private final ShutdownHookHandler shutdownHookHandler;
    @NonNull
    private final AtomicReference<Runnable> shutdownHookOperation;
    @NonNull
    private final BiConsumer<ApplicationServer, ApplicationProcess> lifecycleListenerRegistrar;
    @NonNull
    private final StartInfoPrinter startInfoPrinter;
    @NonNull
    private final Logger logger;

    ApplicationProcessFactory(ApplicationServer applicationServer) {
        this(applicationServer, getLogger(ApplicationProcessFactory.class));
    }

    ApplicationProcessFactory(ApplicationServer applicationServer, Logger logger) {
        this(applicationServer,
             new WorkingDirectoryPreparer(applicationServer),
             new LoggingFrameworkConfigurator(applicationServer),
             new DelegatingServerFactory(applicationServer),
             new TerminatorFactory(applicationServer),
             new ShutdownHookHandler(),
             new AtomicReference<>(),
             LIFECYCLE_LISTENER_REGISTRAR,
             new StartInfoPrinter(applicationServer),
             logger);
    }

    ApplicationProcess createProcess() {
        File applicationWorkingDirectory = workingDirectoryPreparer.prepareWorkingDirectory();
        LoggingFrameworkControl loggingFrameworkControl = loggingFrameworkConfigurator.configureLoggingFramework();
        Server server = delegatingServerFactory.create(applicationWorkingDirectory);
        Runnable shutdownHookRemover = () -> shutdownHookHandler.deregister(shutdownHookOperation.get());
        Terminator terminator = terminatorFactory.create(applicationWorkingDirectory, server, loggingFrameworkControl, shutdownHookRemover);
        ApplicationProcess result = new ApplicationProcess(applicationServer, server::start, terminator);
        shutdownHookOperation.set(() -> beforeProcessShutdown(terminator, result));
        shutdownHookHandler.register(shutdownHookOperation.get());
        lifecycleListenerRegistrar.accept(applicationServer, result);
        startInfoPrinter.printStartText();
        logger.info(INFO_WORKING_DIRECTORY, applicationWorkingDirectory.getAbsolutePath());
        logger.info(INFO_SERVER_USAGE, applicationServer.getIdentifier(), server.getName());
        return result;
    }
}
