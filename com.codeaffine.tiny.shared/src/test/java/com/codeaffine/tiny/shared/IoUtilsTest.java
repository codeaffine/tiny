/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.util.UUID;

import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IoUtilsTest {

    private String directoryNamePrefix;
    private File tmpDir;

    @BeforeEach
    void setUp() {
        directoryNamePrefix = UUID.randomUUID().toString();
        tmpDir = new File(getProperty("java.io.tmpdir"));
    }

    @AfterEach
    void tearDown() {
        IoUtils.deleteDirectory(new File(tmpDir, directoryNamePrefix));
    }

    @Test
    void findFreePort() {
        int actual = IoUtils.findFreePort();

        assertThat(actual).isNotNegative();
    }

    @Test
    void createTemporaryDirectory() {
        File actual = IoUtils.createTemporaryDirectory(directoryNamePrefix);
        actual.deleteOnExit(); // housekeeping only, not part of the test

        assertThat(actual)
            .exists()
            .hasParent(tmpDir)
            .isDirectory();
        assertThat(actual.getName())
            .startsWith(directoryNamePrefix);
    }

    @ParameterizedTest(name = "[{index}] expecting argument ''{0}'' to throw an IllegalArgumentException.")
    @CsvSource({
        "''",
        "^\\/;"
    })
    void createTemporaryDirectoryWithIllegalNameAsDirectoryNamePrefixArgument(String illegalDirectoryNamePrefix) {
        Exception actual = catchException(() -> IoUtils.createTemporaryDirectory(illegalDirectoryNamePrefix));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(illegalDirectoryNamePrefix);
    }

    @Test
    void createTemporaryDirectoryWithNullAsDirectoryNamePrefixArgument() {
        assertThatThrownBy(() -> IoUtils.createTemporaryDirectory(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteDirectory() {
        File directoryToDelete = IoUtils.createTemporaryDirectory(directoryNamePrefix);
        File child = new File(directoryToDelete, "child");
        boolean created = child.mkdirs();

        IoUtils.deleteDirectory(directoryToDelete);

        assertThat(created).isTrue();
        assertThat(directoryToDelete).doesNotExist();
        assertThat(child).doesNotExist();
    }

    @Test
    void deleteDirectoryIfArgumentToDeleteCannotBeDeleted() {
        File unDeletableFile = fakeFileThatCannotBeDeleted();

        Exception actual = catchException(() -> IoUtils.deleteDirectory(unDeletableFile));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
        verify(unDeletableFile, times(2)).exists();  // ensure racing condition workaround is in place
    }

    @Test
    void deleteDirectoryWithNullAsDirectoryArgument() {
        assertThatThrownBy(() -> IoUtils.deleteDirectory(null))
            .isInstanceOf(NullPointerException.class);
    }

    private static File fakeFileThatCannotBeDeleted() {
        return spy(new File("unknown") {
            @Override
            public boolean exists() {
                return true;
            }
        });
    }
}
