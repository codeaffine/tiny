package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
class Messages {

    static final String ERROR_INVALID_METHOD_SIGNATURE = "Invalid method signature: %s.%s(...). "
        + "%s listeners methods have either no parameter or a single parameter of type %s.";
    static final String ERROR_NOTIFYING_STARTED_LISTENER = "Error while notifying started listener:";
    static final String ERROR_NOTIFYING_STOPPING_LISTENER = "Error while notifying stopping listener:";
    static final String ERROR_NOTIFYING_STOPPED_LISTENER = "Error while notifying stopped listener:";
    static final String ERROR_TERMINATING_APPLICATION = "Problems occurred during application shutdown. For details see log mesages.";
    static final String ENFORCING_APPLICATION_TERMINATION = "Enforcing application shutdown.";
    static final String DEBUG_APPLICATION_NOT_HALTED = "Application is not halted.";
    static final String DEBUG_APPLICATION_NOT_RUNNING = "Application is not running.";
}
