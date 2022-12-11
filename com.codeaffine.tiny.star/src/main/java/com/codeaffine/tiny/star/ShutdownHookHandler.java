package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static com.codeaffine.tiny.star.Texts.THREAD_NAME_APPLICATION_SERVER_SHUTDOWN_HOOK;

import static java.lang.Thread.currentThread;

import com.codeaffine.tiny.star.common.Threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;

class ShutdownHookHandler {

    private final Set<Runnable> shutdownOperations;
    private final RuntimeSupplier runtimeSupplier;
    @Getter
    private final Thread shutdownHookThread;

    static class RuntimeSupplier {
        Runtime getRuntime() {
            return Runtime.getRuntime();
        }
    }

    ShutdownHookHandler() {
        this(new RuntimeSupplier());
    }

    ShutdownHookHandler(@NonNull RuntimeSupplier runtimeSupplier) {
        this.shutdownOperations = new HashSet<>();
        this.shutdownHookThread = new Thread(this::executeRegisteredShutdownOperations, THREAD_NAME_APPLICATION_SERVER_SHUTDOWN_HOOK);
        this.runtimeSupplier = runtimeSupplier;
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
        synchronized (shutdownOperations) {
            shutdownOperations.add(shutdownOperation);
            if(shutdownOperations.size() == 1) {
                runtimeSupplier.getRuntime().addShutdownHook(shutdownHookThread);
            }
        }
    }

    void deregister(@NonNull Runnable shutdownOperation) {
        synchronized (shutdownOperations) {
            shutdownOperations.remove(shutdownOperation);
            if(shutdownOperations.isEmpty() && !isShutdownHookThread()) {
                runtimeSupplier.getRuntime().removeShutdownHook(shutdownHookThread);
            }
        }
    }

    private boolean isShutdownHookThread() {
        return currentThread() == shutdownHookThread;
    }

    private void executeRegisteredShutdownOperations() {
        ArrayList<Runnable> operations;
        synchronized (shutdownOperations) {
            operations = new ArrayList<>(shutdownOperations);
        }
        operations.forEach(Threads::saveRun);
    }
}
