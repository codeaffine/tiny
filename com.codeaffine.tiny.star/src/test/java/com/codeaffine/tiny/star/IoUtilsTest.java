package com.codeaffine.tiny.star;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.util.UUID;

import static com.codeaffine.tiny.star.FilesTestHelper.fakeFileThatCannotBeDeleted;
import static com.codeaffine.tiny.star.IoUtils.createTemporayDirectory;
import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.*;

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
        File actual = IoUtils.createTemporayDirectory(directoryNamePrefix);
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
        Throwable actual = catchThrowable(() -> createTemporayDirectory(illegalDirectoryNamePrefix));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(illegalDirectoryNamePrefix);
    }

    @Test
    void createTemporaryDirectoryWithNullAsDirectoryNamePrefixArgument() {
        assertThatThrownBy(() -> createTemporayDirectory(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteDirectory() {
        File directoryToDelete = createTemporayDirectory(directoryNamePrefix);
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

        Throwable actual = catchThrowable(() -> IoUtils.deleteDirectory(unDeletableFile));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(unDeletableFile.toString());
    }

    @Test
    void deleteDirectoryWithNullAsDirectoryArgument() {
        assertThatThrownBy(() -> IoUtils.deleteDirectory(null))
            .isInstanceOf(NullPointerException.class);
    }
}
