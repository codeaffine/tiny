package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
class Messages {

    static final String ERROR_THREAD_HAS_BEEN_INTERRUPTED = "Thread has been interrupted.";
    static final String ERROR_AWAITING_SHUT_DOWN_CLI = "Awaiting shutdown of CLI executor service timed out.";
    static final String ERROR_SHUTING_DOWN_CLI = "Awaiting shutdown of CLI executor has been interrupted.";
    static final String DEBUG_START_SCANNING_FOR_COMMANDS = "Start scanning for commands.";
    static final String DEBUG_DISPATCHING_COMMAND = "Dispatching command: {}";
    static final String DEBUG_END_SCANNING_FOR_COMMANDS = "End scanning for commands.";
}
