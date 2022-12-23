package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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

    void removeCliInstance(ApplicationServer applicationServer, int instanceIdentifier, Map<String, CliCommand> toRemove) {
        synchronized (codeToCommandMap) {
            toRemove.forEach((code, command) -> adaptAndRemoveCliCommand(applicationServer, instanceIdentifier, command));
            cliInstanceCounter--;
            if (cliInstanceCounter == 0) {
                stop();
            }
        }
    }

    int addCliInstance(ApplicationServer applicationServer, Map<String, CliCommand> toAdd) {
        synchronized (codeToCommandMap) {
            toAdd.forEach((code, command) -> adaptAndAddCliCommand(applicationServer, command, cliInstanceIdentifier));
            cliInstanceCounter++;
            if(cliInstanceCounter == 1) {
                start();
            }
            return cliInstanceIdentifier++;
        }
    }

    boolean isRunning() {
        synchronized (codeToCommandMap) {
            return cliInstanceCounter > 0;
        }
    }

    private void start() {
        executor.execute(inputScanner::scanForCommandCode);
    }

    private void stop() {
        inputScanner.cancel();
        executor.stop();
    }

    private void adaptAndAddCliCommand(ApplicationServer applicationServer, CliCommand command, int cliInstanceIdentifier) {
        CliInstanceCommandAdapter adapter = adaptCliCommand(applicationServer, command, cliInstanceIdentifier);
        codeToCommandMap.put(adapter.getCode(), adapter);
    }

    private void adaptAndRemoveCliCommand(ApplicationServer applicationServer, int instanceIdentifier, CliCommand command) {
        CliInstanceCommandAdapter adapter = adaptCliCommand(applicationServer, command, instanceIdentifier);
        codeToCommandMap.remove(adapter.getCode());
    }

    private static CliInstanceCommandAdapter adaptCliCommand(ApplicationServer applicationServer, CliCommand command, int cliInstanceIdentifier) {
        Integer cliInstanceNumber = cliInstanceIdentifier == 0 ? null : cliInstanceIdentifier;
        return new CliInstanceCommandAdapter(applicationServer, command, cliInstanceNumber);
    }
}
