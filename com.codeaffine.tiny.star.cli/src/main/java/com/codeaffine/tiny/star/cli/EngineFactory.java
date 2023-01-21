package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.cli.spi.CliCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class EngineFactory {

    @NonNull
    private final Supplier<ExecutorServiceAdapter> executorFactory;

    EngineFactory() {
        this(() -> new ExecutorServiceAdapter(newCachedThreadPool()));
    }

    Engine createEngine() {
        ExecutorServiceAdapter executor = executorFactory.get();
        Map<String, CliCommand> codeToCommandMap = new HashMap<>();
        CommandDispatcher commandDispatcher = new CommandDispatcher(codeToCommandMap, executor);
        InputScanner inputScanner = new InputScanner(commandDispatcher);
        return new Engine(executor, inputScanner, codeToCommandMap);
    }
}
