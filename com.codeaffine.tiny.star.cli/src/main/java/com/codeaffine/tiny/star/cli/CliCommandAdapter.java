/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class CliCommandAdapter implements CliCommand {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    @Delegate(excludes = Excludes.class)
    private final CliCommand delegate;
    private final int cliInstanceNumber;

    @SuppressWarnings("unused")
    interface Excludes {
        String getCode();
        void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap);
    }

    @Override
    public String getCode() {
        if(cliInstanceNumber > 0) {
            return delegate.getCode() + cliInstanceNumber;
        }
        return delegate.getCode();
    }

    @Override
    public void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap) {
        delegate.execute(this.applicationServer, new HashMap<>(codeToCommandMap));
    }
}
