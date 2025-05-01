/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
import com.codeaffine.tiny.star.cli.test.fixtures.BomCliAvailabilityContract;
import com.codeaffine.tiny.star.test.fixtures.BomUiStartContract;
import com.codeaffine.tiny.star.test.fixtures.BomWorkDirStructureContract;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest
    implements
    BomUiStartContract,
    BomWorkDirStructureContract,
    BomCliAvailabilityContract
{

    @Override
    public void verifyStructure(Path workingDirectory) throws Exception {
        assertThat(workingDirectory.resolve("logs").resolve("application.log")).isRegularFile();
        assertThat(workingDirectory.resolve("rwt-resources")).isNotEmptyDirectory();
    }
}
