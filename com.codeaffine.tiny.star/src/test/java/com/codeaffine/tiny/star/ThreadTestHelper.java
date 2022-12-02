package com.codeaffine.tiny.star;

import static java.lang.Thread.sleep;

public class ThreadTestHelper {

    public static void sleepFor(long millis) {
        try {
            sleep(millis); // NOSONAR
        } catch (InterruptedException shouldNotHappen) {
            throw new IllegalStateException(shouldNotHappen);
        }
    }
}
