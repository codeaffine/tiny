/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import com.codeaffine.tiny.star.cli.spi.CliCommandProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.ApplicationServer.Starting;
import static com.codeaffine.tiny.star.ApplicationServer.Stopped;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

@RequiredArgsConstructor(access = PACKAGE)
public class CommandLineInterface {

    static final AtomicReference<Engine> GLOBAL_ENGINE = new AtomicReference<>();

    static Logger logger = getLogger(CommandLineInterface.class);

    private static final EngineFactory ENGINE_FACTORY = new EngineFactory();

    @NonNull
    private final CliCommandProvider commandProvider;
    @NonNull
    private final AtomicReference<Engine> commandlineEngineHolder;

    private Map<String, CliCommand> instanceCodeToCommandMap;
    private ApplicationServer applicationServer;
    private Integer instanceIdentifier;

    public CommandLineInterface() {
        this(new DelegatingCliCommandProvider());
    }

    public CommandLineInterface(CliCommandProvider cliCommandProvider) {
        this(cliCommandProvider, new AtomicReference<>());
    }

    @Stopped
    public void stopCli() {
        commandlineEngineHolder.updateAndGet(this::doStop);
    }

    @SuppressWarnings("SameReturnValue")
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
        commandlineEngineHolder.updateAndGet(engine -> doStart(engine, applicationServer));
    }

    private Engine doStart(Engine engine, ApplicationServer applicationServer) {
        if(nonNull(engine)) {
            return engine;
        }
        this.applicationServer = applicationServer;
        Engine result = GLOBAL_ENGINE.updateAndGet(CommandLineInterface::ensureGlobalEngineExists);
        instanceCodeToCommandMap = loadCodeToCommandMap();
        instanceIdentifier = result.addCliInstance(applicationServer, instanceCodeToCommandMap);
        printHelpOnStartup(applicationServer, logger);
        return result;
    }

    private static Engine ensureGlobalEngineExists(Engine globalEngine) {
        if (isNull(globalEngine)) {
            return ENGINE_FACTORY.createEngine();
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
        CliCommandAdapter commandAdapter = new CliCommandAdapter(applicationServer, command, instanceIdentifier);
        if(commandAdapter.printHelpOnStartup() && logger.isInfoEnabled()) {
            logger.info(commandAdapter.getDescription(commandAdapter, applicationServer));
        }
    }
}
