/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.basic;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;

import static com.codeaffine.tiny.star.cli.basic.Texts.RUN_DESCRIPTION;
import static com.codeaffine.tiny.star.cli.basic.Texts.RUN_NAME;
import static java.lang.String.format;

class RunCommand implements CliCommand {

    @Override
    public String getCode() {
        return "r";
    }

    @Override
    public String getName() {
        return RUN_NAME;
    }

    @Override
    public String getDescription(CliCommand command, ApplicationServer applicationServer) {
        return format(RUN_DESCRIPTION, command.getCode(), applicationServer.getIdentifier());
    }

    @Override
    public void execute(ApplicationServer applicationServer) {
        applicationServer.start();
    }
}
