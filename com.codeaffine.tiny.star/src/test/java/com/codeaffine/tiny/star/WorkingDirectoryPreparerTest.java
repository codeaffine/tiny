package com.codeaffine.tiny.star;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static com.codeaffine.tiny.star.ApplicationServer.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static com.codeaffine.tiny.star.common.IoUtils.deleteDirectory;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(ApplicationServerTestContext.class)
class WorkingDirectoryPreparerTest {

    public static final String NON_EXISTING_FILE_NAME = "does-not-exist";

    private File workingDirectory;
    @TempDir
    private File tempDirectory;

    @AfterEach
    void tearDown() {
        if(nonNull(workingDirectory) && workingDirectory.exists()) {
            deleteDirectory(workingDirectory);
        }
    }

    @Test
    void prepareWorkingDirectory() {
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {}).build();
        WorkingDirectoryPreparer preparer = new WorkingDirectoryPreparer(applicationServer);

        preparer.prepareWorkingDirectory();
        workingDirectory = new File(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY));

        assertThat(workingDirectory).exists();
    }

    @Test
    void prepareWorkingDirectoryWithWorkingDirectorySettingThatDoesNotExist() {
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {})
            .withWorkingDirectory(new File(NON_EXISTING_FILE_NAME))
            .build();
        WorkingDirectoryPreparer preparer = new WorkingDirectoryPreparer(applicationServer);

        Throwable actual = catchThrowable(preparer::prepareWorkingDirectory);

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(NON_EXISTING_FILE_NAME);
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY))
            .isNull();
    }

    @Test
    void prepareWorkingDirectoryWithWorkingDirectorySettingThatIsFile() throws IOException {
        workingDirectory = tempDirectory;
        File file = new File(workingDirectory, NON_EXISTING_FILE_NAME);
        boolean created = file.createNewFile();
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {})
            .withWorkingDirectory(file)
            .build();
        WorkingDirectoryPreparer preparer = new WorkingDirectoryPreparer(applicationServer);

        Throwable actual = catchThrowable(preparer::prepareWorkingDirectory);

        assertThat(created).isTrue();
        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(NON_EXISTING_FILE_NAME);
        assertThat(System.getProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY))
            .isNull();
    }

    @Test
    void constructWithNullAsApplicationServerArgument() {
        assertThatThrownBy(() -> new WorkingDirectoryPreparer(null))
            .isInstanceOf(NullPointerException.class);
    }
}
