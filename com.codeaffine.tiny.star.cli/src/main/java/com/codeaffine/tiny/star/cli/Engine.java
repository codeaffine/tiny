/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.shared.Synchronizer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class Engine {

    private int cliInstanceIdentifier;
    private int cliInstanceCounter;

    @NonNull
    private final ExecutorServiceAdapter executor;
    @NonNull
    private final InputScanner inputScanner;
    @NonNull
    private final Map<String, CliCommand> codeToCommandMap;
    @NonNull
    private final Synchronizer synchronizer;

    Engine(ExecutorServiceAdapter executor, InputScanner inputScanner, Map<String, CliCommand> codeToCommandMap) {
        this(executor, inputScanner, codeToCommandMap, new Synchronizer());
    }

    void removeCliInstance(ApplicationServer applicationServer, int instanceIdentifier, Map<String, CliCommand> toRemove) {
        synchronizer.execute(() -> doRemoveCliInstance(applicationServer, instanceIdentifier, toRemove));
    }

    Integer addCliInstance(ApplicationServer applicationServer, Map<String, CliCommand> toAdd) {
        return synchronizer.execute(() -> doAddCliInstance(applicationServer, toAdd));
    }

    boolean isRunning() {
        return Optional.ofNullable(synchronizer.execute(() -> cliInstanceCounter > 0))
            .orElse(false);
    }

    private void doRemoveCliInstance(ApplicationServer applicationServer, int instanceIdentifier, Map<String, CliCommand> toRemove) {
        toRemove.forEach((code, command) -> adaptAndRemoveCliCommand(applicationServer, instanceIdentifier, command));
        cliInstanceCounter--;
        if (cliInstanceCounter == 0) {
            stop();
        }
    }

    private int doAddCliInstance(ApplicationServer applicationServer, Map<String, CliCommand> toAdd) {
        toAdd.forEach((code, command) -> adaptAndAddCliCommand(applicationServer, command, cliInstanceIdentifier));
        cliInstanceCounter++;
        if(cliInstanceCounter == 1) {
            start();
        }
        return cliInstanceIdentifier++;
    }

    private void start() {
        executor.execute(inputScanner::scanForCommandCode);
    }

    private void stop() {
        inputScanner.cancel();
        executor.stop();
    }

    private void adaptAndAddCliCommand(ApplicationServer applicationServer, CliCommand command, int cliInstanceIdentifier) {
        CliCommandAdapter adapter = adaptCliCommand(applicationServer, command, cliInstanceIdentifier);
        codeToCommandMap.put(adapter.getCode(), adapter);
    }

    private void adaptAndRemoveCliCommand(ApplicationServer applicationServer, int instanceIdentifier, CliCommand command) {
        CliCommandAdapter adapter = adaptCliCommand(applicationServer, command, instanceIdentifier);
        codeToCommandMap.remove(adapter.getCode());
    }

    private static CliCommandAdapter adaptCliCommand(ApplicationServer applicationServer, CliCommand command, int cliInstanceIdentifier) {
        return new CliCommandAdapter(applicationServer, command, cliInstanceIdentifier);
    }
}
