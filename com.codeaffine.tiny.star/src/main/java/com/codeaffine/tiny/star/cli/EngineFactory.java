package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PACKAGE;

import static java.util.concurrent.Executors.newCachedThreadPool;

import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
