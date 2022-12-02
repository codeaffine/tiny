package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationInstance.State.RUNNING;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
class ShutdownHookHandler {

    static void beforeProcessShutdown(@NonNull Terminator terminator, @NonNull ApplicationInstance result) {
        terminator.setShutdownHookExecution(true);
        if (RUNNING == result.getState()) {
            result.stop();
        } else {
            terminator.deleteWorkingDirectory();
        }
    }
}
