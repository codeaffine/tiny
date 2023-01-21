package com.codeaffine.tiny.star.cli;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_AWAITING_SHUT_DOWN_CLI = "Awaiting shutdown of CLI executor service timed out.";
    static final String ERROR_SHUTTING_DOWN_CLI = "Awaiting shutdown of CLI executor has been interrupted.";
    static final String DEBUG_START_SCANNING_FOR_COMMANDS = "Start scanning for commands.";
    static final String DEBUG_DISPATCHING_COMMAND = "Dispatching command: {}";
    static final String DEBUG_END_SCANNING_FOR_COMMANDS = "End scanning for commands.";
    static final String STD_OUT_UNKNOWN_COMMAND = "Unknown command [%s]. Type [%s] for help.%n";
    static final String SHUTDOWN_THREAD_NAME = "Shutdown Handler of Command Line Interface";
}