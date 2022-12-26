package com.codeaffine.tiny.star;

import lombok.NonNull;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.star.ApplicationProcess.StopMode.ENFORCED;
import static com.codeaffine.tiny.star.ApplicationProcess.StopMode.NORMAL;
import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.State.*;
import static com.codeaffine.tiny.star.Texts.*;
import static com.codeaffine.tiny.star.common.Reflections.Mode.FORWARD_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.star.common.Reflections.extractExceptionToReport;
import static org.slf4j.LoggerFactory.getLogger;

class ApplicationProcess {

    private static final long OBSERVER_NOTIFICATION_TIMEOUT = 5000;

    private final ObserverRegistry<ApplicationServer> observerRegistry;
    private final AtomicReference<State> state;
    private final Runnable terminator;
    private final Runnable starter;
    private final Logger logger;

    enum StopMode { NORMAL, ENFORCED }

    static class LifecycleException extends RuntimeException {
        LifecycleException(@SuppressWarnings("SameParameterValue") String message) { super(message); }
        LifecycleException(Throwable cause) { super(cause); }
    }

    ApplicationProcess(ApplicationServer applicationServer, Runnable starter, Runnable terminator) {
        this(applicationServer, starter, terminator, getLogger(ApplicationProcess.class));
    }

    ApplicationProcess(@NonNull ApplicationServer applicationServer, @NonNull Runnable starter, @NonNull Runnable terminator, @NonNull Logger logger) {
        this.observerRegistry = new ObserverRegistry<>(
            applicationServer,
            ApplicationServer.class,
            OBSERVER_NOTIFICATION_TIMEOUT,
            Starting.class, Started.class, Stopping.class, Stopped.class
        );
        this.state = new AtomicReference<>(HALTED);
        this.terminator = terminator;
        this.starter = starter;
        this.logger = logger;
    }

    void registerLifecycleListener(Object lifecycleListener) {
        observerRegistry.registerObserver(lifecycleListener);
    }

    void deregisterLifecycleListener(Object lifecycleListener) {
        observerRegistry.deregisterObserver(lifecycleListener);
    }

    void start() {
        if(state.compareAndSet(HALTED, STARTING)) {
            doStart();
        } else {
            logger.debug(DEBUG_APPLICATION_NOT_HALTED);
        }
    }

    private void doStart() {
        observerRegistry.notifyObservers(Starting.class, exception -> {
            throw extractExceptionToReport(exception, LifecycleException::new, FORWARD_RUNTIME_EXCEPTIONS);
        });
        starter.run();
        observerRegistry.notifyObservers(Started.class, exception -> {
            throw handleExceptionOnStartedListener(exception);
        });
        state.set(RUNNING);
    }

    private RuntimeException handleExceptionOnStartedListener(Exception exception) {
        RuntimeException toRethrow = extractExceptionToReport(exception, LifecycleException::new, FORWARD_RUNTIME_EXCEPTIONS);
        logger.error(ERROR_NOTIFYING_STARTED_LISTENER, toRethrow);
        logger.error(ENFORCING_APPLICATION_TERMINATION);
        state.set(RUNNING);
        stopInternal(ENFORCED);
        return toRethrow;
    }

    State getState() {
        return state.get();
    }

    void stop() {
        stopInternal(NORMAL);
    }

    private void stopInternal(StopMode stopMode) {
        if (state.compareAndSet(RUNNING, STOPPING)) {
            doStop(stopMode);
        } else {
            logger.debug(DEBUG_APPLICATION_NOT_RUNNING);
        }
    }

    private void doStop(StopMode stopMode) {
        AtomicBoolean soundShutdown = new AtomicBoolean(true);
        state.set(STOPPING);
        observerRegistry.notifyObservers(Stopping.class, exception -> handleExceptionOnShutdown(soundShutdown, ERROR_NOTIFYING_STOPPING_LISTENER, exception));
        terminator.run();
        state.set(HALTED);
        observerRegistry.notifyObservers(Stopped.class, exception -> handleExceptionOnShutdown(soundShutdown, ERROR_NOTIFYING_STOPPED_LISTENER, exception));
        if (NORMAL == stopMode && !soundShutdown.get()) {
            throw new LifecycleException(ERROR_TERMINATING_APPLICATION);
        }
    }

    private void handleExceptionOnShutdown(AtomicBoolean soundShutdown, String logMessagePattern, Exception exception) {
        soundShutdown.set(false);
        logger.error(logMessagePattern, extractExceptionToReport(exception, LifecycleException::new, FORWARD_RUNTIME_EXCEPTIONS));
    }
}
