/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PACKAGE;

@SuppressWarnings("BusyWait")
@RequiredArgsConstructor(access = PACKAGE)
class CancelableInputStream extends InputStream {

    static final long SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS = 70;
    static final int NO_MORE_DATA = -1;

    @NonNull
    @Delegate(excludes = Excludes.class)
    private final InputStream delegate;
    @NonNull
    private final InputStream cancelSignalInputStream;
    private final long suspendedTimeInMillisBetweenDataAvailabilityChecks;

    private volatile boolean canceled;

    @SuppressWarnings("unused")
    interface Excludes {
        int read(byte[] buffer, int off, int end);
        void close();
    }

    CancelableInputStream(@NonNull InputStream delegate, @NonNull String cancelSignal) {
        this(delegate, new ByteArrayInputStream(cancelSignal.getBytes(UTF_8)), SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte @NonNull [] buffer, int off, int end) throws IOException {
        try {
            return doRead(buffer, off, end);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            cancel();
            return NO_MORE_DATA;
        }
    }

    void cancel() {
        canceled = true;
    }

    private int doRead(byte[] buffer, int off, int end) throws IOException, InterruptedException {
        waitBusyTillBytesAvailable();
        if (!canceled) {
            return delegate.read(buffer, off, end);
        } else {
            return cancelSignalInputStream.read(buffer, off, end);
        }
    }

    private void waitBusyTillBytesAvailable() throws IOException, InterruptedException {
        while (delegate.available() == 0 && !canceled) {
            //noinspection BusyWait
            sleep(suspendedTimeInMillisBetweenDataAvailabilityChecks); // ignore warning: unfortunately busy-waiting is the way to go...
        }
    }
}
