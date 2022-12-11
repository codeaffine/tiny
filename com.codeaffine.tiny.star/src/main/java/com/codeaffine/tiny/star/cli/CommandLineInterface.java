package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.ApplicationServer.Starting;
import static com.codeaffine.tiny.star.ApplicationServer.Stopped;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import org.slf4j.Logger;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
public class CommandLineInterface {

    @NonNull
    private final DelegatingCliCommandProvider commandProvider;
    @NonNull
    private final Supplier<ExecutorServiceAdapter> executorFactory;
    @NonNull
    private final AtomicReference<Engine> commandlineEngineHolder;
    @NonNull
    private final Logger logger;

    public CommandLineInterface() {
        this(new DelegatingCliCommandProvider(),
             () -> new ExecutorServiceAdapter(newCachedThreadPool()),
             new AtomicReference<>(),
             getLogger(CommandLineInterface.class));
    }

    @Stopped
    public void stopCli() {
        commandlineEngineHolder.updateAndGet(CommandLineInterface::doStop);
    }

    private static Engine doStop(Engine engine) {
        if (nonNull(engine)) {
            engine.stop();
        }
        return null;
    }

    @Starting
    public void startCli(ApplicationServer applicationServer) {
        commandlineEngineHolder.updateAndGet(engine -> doStart(engine, applicationServer));
    }

    private Engine doStart(Engine engine, ApplicationServer applicationServer) {
        if(nonNull(engine)) {
            return engine;
        }
        Map<String, CliCommand> codeToCommandMap = loadCodeToCommandMap();
        printHelpOnStartup(applicationServer, codeToCommandMap, logger);
        Engine result = createEngine(applicationServer, codeToCommandMap);
        result.start();
        return result;
    }

    private Engine createEngine(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap) {
        ExecutorServiceAdapter executor = executorFactory.get();
        CommandDispatcher commandDispatcher = new CommandDispatcher(applicationServer, codeToCommandMap, executor);
        InputScanner inputScanner = new InputScanner(commandDispatcher);
        return new Engine(executor, inputScanner);
    }

    private Map<String, CliCommand> loadCodeToCommandMap() {
        return commandProvider.getCliCommands()
            .stream()
            .collect(toMap(CliCommand::getCode, identity()));
    }

    private static void printHelpOnStartup(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap, Logger logger) {
        codeToCommandMap.values()
            .forEach(command -> printHelpOnStartup(applicationServer, command, logger));
    }

    private static void printHelpOnStartup(ApplicationServer applicationServer, CliCommand command, Logger logger) {
        if(command.printHelpOnStartup()) {
            logger.info(command.getDescription(command, applicationServer));
        }
    }
}
