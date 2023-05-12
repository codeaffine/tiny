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
