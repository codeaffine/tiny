package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.common.Synchronizer;
import com.codeaffine.tiny.star.spi.CliCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

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
        synchronizer.execute(() -> doRemmoveCliInstance(applicationServer, instanceIdentifier, toRemove));
    }

    Integer addCliInstance(ApplicationServer applicationServer, Map<String, CliCommand> toAdd) {
        return synchronizer.execute(() -> doAddCliInstance(applicationServer, toAdd));
    }

    Boolean isRunning() {
        return synchronizer.execute(() -> cliInstanceCounter > 0);
    }

    private void doRemmoveCliInstance(ApplicationServer applicationServer, int instanceIdentifier, Map<String, CliCommand> toRemove) {
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
