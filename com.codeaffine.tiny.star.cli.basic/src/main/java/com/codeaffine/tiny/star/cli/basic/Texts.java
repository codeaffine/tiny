/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String STD_OUT_AVAILABLE_COMMANDS_DESCRIPTION = "Available commands:\n(name [keycode]: description)\n";
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
