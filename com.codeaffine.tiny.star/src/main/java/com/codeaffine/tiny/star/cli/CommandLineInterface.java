package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.ApplicationServer.Starting;
import static com.codeaffine.tiny.star.ApplicationServer.Stopped;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import org.slf4j.Logger;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
public class CommandLineInterface {

    static final AtomicReference<Engine> GLOBAL_ENGINE = new AtomicReference<>();

    @NonNull
    private final DelegatingCliCommandProvider commandProvider;
    @NonNull
    private final AtomicReference<Engine> commandlineEngineHolder;
    @NonNull
    private final Logger logger;

    private Map<String, CliCommand> instanceCodeToCommandMap;
    private ApplicationServer applicationServer;
    private Integer instanceIdentifier;

    public CommandLineInterface() {
        this(new DelegatingCliCommandProvider(), new AtomicReference<>(), getLogger(CommandLineInterface.class));
    }

    @Stopped
    public void stopCli() {
        commandlineEngineHolder.updateAndGet(this::doStop);
    }

    private Engine doStop(Engine engine) {
        if (nonNull(engine)) {
            engine.removeCliInstance(applicationServer, instanceIdentifier, instanceCodeToCommandMap);
            GLOBAL_ENGINE.updateAndGet(CommandLineInterface::removeUnusedGlobalEngine);
        }
        return null;
    }

    private static Engine removeUnusedGlobalEngine(Engine globalEngine) {
        if(globalEngine.isRunning()) {
            return globalEngine;
        }
        return null;
    }

    @Starting
    public void startCli(ApplicationServer applicationServer) {
        this.applicationServer = applicationServer;
        commandlineEngineHolder.updateAndGet(engine -> doStart(engine, applicationServer));
    }

    private Engine doStart(Engine engine, ApplicationServer applicationServer) {
        if(nonNull(engine)) {
            return engine;
        }
        Engine result = GLOBAL_ENGINE.updateAndGet(CommandLineInterface::ensureGlobalEngineExists);
        instanceCodeToCommandMap = loadCodeToCommandMap();
        instanceIdentifier = result.addCliInstance(applicationServer, instanceCodeToCommandMap);
        printHelpOnStartup(applicationServer, logger);
        return result;
    }

    private static Engine ensureGlobalEngineExists(Engine globalEngine) {
        if (isNull(globalEngine)) {
            return new EngineFactory().createEngine();
        }
        return globalEngine;
    }

    private Map<String, CliCommand> loadCodeToCommandMap() {
        return commandProvider.getCliCommands()
            .stream()
            .collect(toMap(CliCommand::getCode, identity()));
    }

    private void printHelpOnStartup(ApplicationServer applicationServer, Logger logger) {
        instanceCodeToCommandMap.values()
            .forEach(command -> printHelpOnStartup(applicationServer, command, logger));
    }

    private void printHelpOnStartup(ApplicationServer applicationServer, CliCommand command, Logger logger) {
        CliInstanceCommandAdapter commandAdapter = new CliInstanceCommandAdapter(applicationServer, command, instanceIdentifier);
        if(commandAdapter.printHelpOnStartup()) {
            logger.info(commandAdapter.getDescription(commandAdapter, applicationServer));
        }
    }
}
