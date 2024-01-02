/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;

import static java.lang.String.format;

class TestCliCommand implements CliCommand {

    static final String COMMAND = "Command";
    static final String CODE = "c";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getName() {
        return COMMAND;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationServer) {
        return format("%s %s", command.getCode(), applicationServer.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationServer) {
        applicationServer.stop();
    }

    @Override
    public boolean isHelpCommand() {
        return true;
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }
}
