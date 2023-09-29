/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.shared.Synchronizer;
import com.codeaffine.tiny.shared.Threads;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static com.codeaffine.tiny.star.Texts.THREAD_NAME_APPLICATION_SERVER_SHUTDOWN_HOOK;
import static java.lang.Thread.currentThread;

class ShutdownHookHandler {

    private final Set<Runnable> shutdownOperations;
    private final RuntimeSupplier runtimeSupplier;
    @Getter
    private final Thread shutdownHookThread;
    private final Synchronizer synchronizer;
    private final AtomicBoolean concluded;

    static class RuntimeSupplier {
        Runtime getRuntime() {
            return Runtime.getRuntime();
        }
    }

    ShutdownHookHandler() {
        this(new RuntimeSupplier(), new Synchronizer());
    }

    ShutdownHookHandler(@NonNull RuntimeSupplier runtimeSupplier, @NonNull Synchronizer synchronizer) {
        this.shutdownOperations = new HashSet<>();
        this.shutdownHookThread = new Thread(this::executeRegisteredShutdownOperations, THREAD_NAME_APPLICATION_SERVER_SHUTDOWN_HOOK);
        this.runtimeSupplier = runtimeSupplier;
        this.synchronizer = synchronizer;
        this.concluded = new AtomicBoolean(false);
    }

    static void beforeProcessShutdown(@NonNull Terminator terminator, @NonNull ApplicationProcess process) {
        terminator.setShutdownHookExecution(true);
        if (RUNNING == process.getState()) {
            process.stop();
        } else {
            terminator.deleteWorkingDirectory();
        }
    }

    void register(@NonNull Runnable shutdownOperation) {
        synchronizer.execute(() -> doRegister(shutdownOperation));
    }

    void deregister(@NonNull Runnable shutdownOperation) {
        synchronizer.execute(() -> doDeregister(shutdownOperation));
    }

    private void doRegister(Runnable shutdownOperation) {
        shutdownOperations.add(shutdownOperation);
        if(shutdownOperations.size() == 1) {
            runtimeSupplier.getRuntime().addShutdownHook(shutdownHookThread);
        }
    }

    private void doDeregister(Runnable shutdownOperation) {
        shutdownOperations.remove(shutdownOperation);
        if(shutdownOperations.isEmpty() && !isShutdownHookThread() && !concluded.get()) {
            runtimeSupplier.getRuntime().removeShutdownHook(shutdownHookThread);
        }
    }

    private boolean isShutdownHookThread() {
        return currentThread() == shutdownHookThread;
    }

    private void executeRegisteredShutdownOperations() {
        synchronizer.execute(this::cloneFinalShutdownOperationsList)
            .forEach(Threads::saveRun);
    }

    private List<Runnable> cloneFinalShutdownOperationsList() {
        concluded.set(true);
        return new ArrayList<>(shutdownOperations);
    }
}
