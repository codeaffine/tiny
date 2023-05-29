/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.shared.DaemonThreadFactory;
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

    private static final String THREAD_NAME_PREFIX = "CLI-Engine-Thread";

    @NonNull
    private final Supplier<ExecutorServiceAdapter> executorFactory;

    EngineFactory() {
        this(() -> new ExecutorServiceAdapter(newCachedThreadPool(new DaemonThreadFactory(THREAD_NAME_PREFIX))));
    }

    Engine createEngine() {
        ExecutorServiceAdapter executor = executorFactory.get();
        Map<String, CliCommand> codeToCommandMap = new HashMap<>();
        CommandDispatcher commandDispatcher = new CommandDispatcher(codeToCommandMap, executor);
        InputScanner inputScanner = new InputScanner(commandDispatcher);
        return new Engine(executor, inputScanner, codeToCommandMap);
    }

}
