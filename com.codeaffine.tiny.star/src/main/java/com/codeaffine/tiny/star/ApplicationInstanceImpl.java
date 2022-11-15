package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationInstanceImpl.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationInstanceImpl.State.RUNNING;
import static com.codeaffine.tiny.star.ApplicationInstanceImpl.State.STARTING;
import static com.codeaffine.tiny.star.ApplicationInstanceImpl.State.STOPPING;
import static com.codeaffine.tiny.star.ApplicationInstanceImpl.StopMode.ENFORCED;
import static com.codeaffine.tiny.star.ApplicationInstanceImpl.StopMode.NORMAL;
import static com.codeaffine.tiny.star.Messages.DEBUG_APPLICATION_NOT_HALTED;
import static com.codeaffine.tiny.star.Messages.DEBUG_APPLICATION_NOT_RUNNING;
import static com.codeaffine.tiny.star.Messages.ENFORCING_APPLICATION_TERMINATION;
import static com.codeaffine.tiny.star.Messages.ERROR_NOTIFYING_STARTED_LISTENER;
import static com.codeaffine.tiny.star.Messages.ERROR_NOTIFYING_STOPPED_LISTENER;
import static com.codeaffine.tiny.star.Messages.ERROR_NOTIFYING_STOPPING_LISTENER;
import static com.codeaffine.tiny.star.Messages.ERROR_TERMINATING_APPLICATION;
import static com.codeaffine.tiny.star.Reflections.extractExceptionToReport;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;

class ApplicationInstanceImpl implements ApplicationInstance {

    private final ObserverRegistry<ApplicationInstance> observerRegistry;
    private final AtomicReference<State> state;
    private final Runnable terminator;
    private final Runnable starter;
    private final Logger logger;
    @Getter
    private final String name;

    enum StopMode { NORMAL, ENFORCED }
    enum State { STARTING, RUNNING, STOPPING, HALTED }

    static class LifecycleException extends RuntimeException {
        LifecycleException(String message) { super(message); }
        LifecycleException(Throwable cause) { super(cause); }
    }

    ApplicationInstanceImpl(String name, Runnable starter, Runnable terminator) {
        this(name, starter, terminator, getLogger(ApplicationInstanceImpl.class));
    }

    ApplicationInstanceImpl(@NonNull String name, @NonNull Runnable starter, @NonNull Runnable terminator, @NonNull Logger logger) {
        this.observerRegistry = new ObserverRegistry<>(this, ApplicationInstance.class, Starting.class, Started.class, Stopping.class, Stopped.class);
        this.state = new AtomicReference<>(HALTED);
        this.terminator = terminator;
        this.starter = starter;
        this.logger = logger;
        this.name = name;
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
            throw extractExceptionToReport(exception, LifecycleException::new);
        });
        starter.run();
        observerRegistry.notifyObservers(Started.class, exception -> {
            throw handleExceptionOnStartedListener(exception);
        });
        state.set(RUNNING);
    }

    private RuntimeException handleExceptionOnStartedListener(Exception exception) {
        RuntimeException toRethrow = extractExceptionToReport(exception, LifecycleException::new);
        logger.error(ERROR_NOTIFYING_STARTED_LISTENER, toRethrow);
        logger.error(ENFORCING_APPLICATION_TERMINATION);
        state.set(RUNNING);
        stop(ENFORCED);
        return toRethrow;
    }

    @Override
    public void stop() {
        stop(NORMAL);
    }

    void stop(StopMode stopMode) {
        if(state.compareAndSet(RUNNING, STOPPING)) {
            doStop(stopMode);
        } else {
            logger.debug(DEBUG_APPLICATION_NOT_RUNNING);
        }
    }

    private void doStop(StopMode stopMode) {
        AtomicBoolean soundShutdown = new AtomicBoolean(true);
        observerRegistry.notifyObservers(Stopping.class, exception -> handleExceptionOnShutdown(soundShutdown, ERROR_NOTIFYING_STOPPING_LISTENER, exception));
        terminator.run();
        observerRegistry.notifyObservers(Stopped.class, exception -> handleExceptionOnShutdown(soundShutdown, ERROR_NOTIFYING_STOPPED_LISTENER, exception));
        state.set(HALTED);
        if (NORMAL == stopMode && !soundShutdown.get()) {
            throw new LifecycleException(ERROR_TERMINATING_APPLICATION);
        }
    }

    private void handleExceptionOnShutdown(AtomicBoolean soundShutdown, String logMessagePattern, Exception exception) {
        soundShutdown.set(false);
        logger.error(logMessagePattern, extractExceptionToReport(exception, LifecycleException::new));
    }
}
