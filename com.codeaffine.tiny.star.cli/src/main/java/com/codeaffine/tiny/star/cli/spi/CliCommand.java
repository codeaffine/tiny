/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.spi;

import com.codeaffine.tiny.star.ApplicationServer;

import java.util.Map;

public interface CliCommand {

    String getCode();
    String getName();
    String getDescription(CliCommand command, ApplicationServer applicationServer);

    default boolean printHelpOnStartup() {
        return false;
    }

    default boolean isHelpCommand() {
        return false;
    }

    default void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap) {
        execute(applicationServer);
    }

    default void execute(ApplicationServer applicationServer) {
        execute();
    }

    default void execute() {
    }
}
