/**
 * <p>Copyright (c) 2022-2025 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli.test.fixtures;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BOM test contract that checks that the CLI is available
 */
public interface BomCliAvailabilityContract {

    @Test
    default void cliAvailability() throws IOException {
        Path logFile = Paths.get("build/app-logs/app-output.log");
        String logContent = readString(logFile);

        assertThat(logContent).contains("Type h to list available command descriptions.");
    }
}
