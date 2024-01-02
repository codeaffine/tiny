/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaemonThreadFactoryTest {

    private static final String THREAD_NAME_PREFIX = "threadNamePrefix";

    @Test
    void createThreadAndStartIt() throws InterruptedException {
        DaemonThreadFactory factory = new DaemonThreadFactory(THREAD_NAME_PREFIX);
        AtomicReference<Thread> threadCaptor = new AtomicReference<>();

        Thread thread = factory.newThread(() -> threadCaptor.set(Thread.currentThread()));
        thread.start();
        thread.join();
        Thread actual = threadCaptor.get();

        assertThat(actual)
            .isSameAs(thread)
            .satisfies(capturedThread -> assertThat(capturedThread.getName()).startsWith(THREAD_NAME_PREFIX))
            .satisfies(capturedThread -> assertThat(capturedThread.getName()).endsWith("1"))
            .satisfies(capturedThread -> assertThat(capturedThread.isDaemon()).isTrue());
    }

    @Test
    void createMultipleThreads() {
        DaemonThreadFactory factory = new DaemonThreadFactory(THREAD_NAME_PREFIX);

        Thread actual1 = factory.newThread(() -> {});
        Thread actual2 = factory.newThread(() -> {});

        assertThat(actual1.getName()).endsWith("1");
        assertThat(actual2.getName()).endsWith("2");
    }

    @Test
    void newThreadWithConditionAsRunnableArgument() {
        DaemonThreadFactory factory = new DaemonThreadFactory(THREAD_NAME_PREFIX);

        assertThatThrownBy(() -> factory.newThread(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsThreadNamePrefixArgument() {
        assertThatThrownBy(() -> new DaemonThreadFactory(null))
            .isInstanceOf(NullPointerException.class);
    }
}
