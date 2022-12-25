package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.common.Synchronizer;
import com.codeaffine.tiny.star.common.Threads;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.codeaffine.tiny.star.ApplicationServer.State.RUNNING;
import static com.codeaffine.tiny.star.Texts.THREAD_NAME_APPLICATION_SERVER_SHUTDOWN_HOOK;
import static java.lang.Thread.currentThread;

class ShutdownHookHandler {

    private final Set<Runnable> shutdownOperations;
    private final RuntimeSupplier runtimeSupplier;
    @Getter
    private final Thread shutdownHookThread;
    private final Synchronizer synchronizer;

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
        if(shutdownOperations.isEmpty() && !isShutdownHookThread()) {
            runtimeSupplier.getRuntime().removeShutdownHook(shutdownHookThread);
        }
    }

    private boolean isShutdownHookThread() {
        return currentThread() == shutdownHookThread;
    }

    private void executeRegisteredShutdownOperations() {
        synchronizer.execute(() -> new ArrayList<>(shutdownOperations))
                .forEach(Threads::saveRun);
    }
}
