package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.ApplicationServer.State;
import lombok.NoArgsConstructor;

import static com.codeaffine.tiny.star.common.Threads.sleepFor;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class ApplicationServerCompatibilityContractUtil {

    private static final long MILLIS_BETWEEN_RETRIES = 100L;
    private static final int RETRIES = 20;

    static final long MAX_RETRY_DURATION = RETRIES * MILLIS_BETWEEN_RETRIES / 1000;

    static void awaitState(ApplicationServer applicationServer, State expectedState) {
        awaitState(applicationServer, expectedState, RETRIES, MILLIS_BETWEEN_RETRIES);
    }

    static void awaitState(ApplicationServer applicationServer, State expectedState, int maxRetries, long millisBetweenRetries) {
        State state = applicationServer.getState();
        int retries = 0;
        while (expectedState != state && retries <= maxRetries) {
            retries++;
            sleepFor(millisBetweenRetries);
            state = applicationServer.getState();
        }
    }
}
