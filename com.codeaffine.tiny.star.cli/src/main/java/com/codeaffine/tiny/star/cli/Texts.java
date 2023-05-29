/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
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
