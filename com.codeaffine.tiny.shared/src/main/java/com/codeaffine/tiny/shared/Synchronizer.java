/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NonNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Synchronizer is a utility class that provides a way to execute operations
 * in a thread-safe manner using a lock.
 * <p> It allows you to execute both {@link Runnable} and {@link Supplier} operations
 * while ensuring that the lock is acquired and released properly.</p>
 *
 * <p>Although, the {@link Synchronizer} does not much clue code reduction it
 * eases class structuring for testing purposes. Being a dependent-on-component it
 * can be injected into the class that needs to have synchronized sections and can be
 * replaced by a stub at test time. Therefore, the accompanying test fixture provides a
 * helper class that can be used to stub the {@link Synchronizer}.</p>
 */
public class Synchronizer {

    private final Lock lock;

    /**
     * Creates a new instance of Synchronizer.
     */
    public Synchronizer() {
        this(new ReentrantLock());
    }

    Synchronizer(Lock lock) {
        this.lock = lock;
    }

    /**
     * Executes the given operation while holding the lock.
     *
     * @param operation the operation to execute
     */
    public void execute(@NonNull Runnable operation) {
        lock.lock();
        try {
            operation.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes the given operation while holding the lock and returns the result.
     *
     * @param operation the operation to execute
     * @param <T>       the type of the result
     * @return the result of the operation
     */
    public <T> T execute(@NonNull Supplier<T> operation) {
        lock.lock();
        try {
            return operation.get();
        } finally {
            lock.unlock();
        }
    }
}
