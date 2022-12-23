package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_AWAITING_SHUT_DOWN_CLI = "Awaiting shutdown of CLI executor service timed out.";
    static final String ERROR_SHUTTING_DOWN_CLI = "Awaiting shutdown of CLI executor has been interrupted.";
    static final String DEBUG_START_SCANNING_FOR_COMMANDS = "Start scanning for commands.";
    static final String DEBUG_DISPATCHING_COMMAND = "Dispatching command: {}";
    static final String DEBUG_END_SCANNING_FOR_COMMANDS = "End scanning for commands.";
    static final String STD_OUT_UNKNOWN_COMMAND = "Unknown command [%s]. Type [%s] for help.%n";
    static final String STD_OUT_AVAILABLE_COMMANDS_DESCRIPTION = "Available commands:\n(name [keycode]: description)\n";
    static final String SHUTDOWN_THREAD_NAME = "Shutdown Handler of Command Line Interface";
    static final String QUIT_DESCRIPTION = "Type %s to stop %s application server.";
    static final String QUIT_NAME = "Quit";
    static final String HELP_DESCRIPTION = "Type %s to list available command descriptions.";
    static final String RUN_NAME = "run";
    static final String RUN_DESCRIPTION = "Type %s to start %s application server.";
    static final String HELP_NAME = "Help";
    static final String STATE_DESCRIPTION = "Type %s to show the state information of the %s application server.";
    static final String STATE_NAME = "State";
    static final String STD_OUT_STATE_INFO = "State of the application server %s: %s%n";
}
