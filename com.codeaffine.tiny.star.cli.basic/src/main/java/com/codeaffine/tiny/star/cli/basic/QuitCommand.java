/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;

import static com.codeaffine.tiny.star.cli.basic.Texts.QUIT_DESCRIPTION;
import static com.codeaffine.tiny.star.cli.basic.Texts.QUIT_NAME;
import static java.lang.String.format;

class QuitCommand implements CliCommand {

    @Override
    public String getCode() {
        return "q";
    }

    @Override
    public String getName() {
        return QUIT_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationInstance) {
        return format(QUIT_DESCRIPTION, command.getCode(), applicationInstance.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationInstance) {
        applicationInstance.stop();
    }

    @Override
    public boolean printHelpOnStartup() {
        return true;
    }
}
