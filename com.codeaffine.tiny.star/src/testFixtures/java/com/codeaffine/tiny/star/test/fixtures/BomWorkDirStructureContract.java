/**
 * <p>Copyright (c) 2022-2025 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.test.fixtures;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isDirectory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BOM test contract that checks that the application working directory
 * exists and contains the expected files and directories.
 */
public interface BomWorkDirStructureContract {

  /** hard-coded path to the application working directory for bom integration tests. */
  String BUILD_APP_WORKDIR = "build/app-workdir";

  @Test
  default void workDirStructure() throws Exception {
    Path workDir = Paths.get(BUILD_APP_WORKDIR);

    assertThat(isDirectory(workDir)).isTrue();

    verifyStructure(workDir);
  }

  /**
   * Verifies the expected contents of the application working directory.
   * Must be implemented by the concrete test.
   *
   * @param workingDirectory the test applications work directory (e.g. build/app-workdir)
   * @throws Exception problems during verification
   */
  void verifyStructure(Path workingDirectory) throws Exception;
}
