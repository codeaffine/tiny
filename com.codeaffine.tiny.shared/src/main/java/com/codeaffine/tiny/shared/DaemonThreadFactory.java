/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DaemonThreadFactory implements ThreadFactory {

    @NonNull
    private final String threadNamePrefix;

    private final AtomicInteger poolThreadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread result = new Thread(runnable, format("%s-%s", threadNamePrefix, poolThreadNumber.getAndIncrement()));
        result.setDaemon(true);
        return result;
    }

}
