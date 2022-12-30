package com.codeaffine.tiny.star;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;

import static com.codeaffine.tiny.star.ApplicationServer.SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.Texts.ERROR_GIVEN_WORKING_DIRECTORY_DOES_NOT_EXIST;
import static com.codeaffine.tiny.star.Texts.ERROR_GIVEN_WORKING_DIRECTORY_FILE_IS_NOT_A_DIRECTORY;
import static com.codeaffine.tiny.star.common.IoUtils.createTemporayDirectory;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class WorkingDirectoryPreparer {

    @NonNull
    private final ApplicationServer applicationServer;

    File prepareWorkingDirectory() {
        File result = applicationServer.workingDirectory;
        if (isNull(applicationServer.workingDirectory)) {
            result = createTemporayDirectory(applicationServer.getIdentifier());
        } else if (!result.exists()) {
            throw new IllegalArgumentException(format(ERROR_GIVEN_WORKING_DIRECTORY_DOES_NOT_EXIST, result.getAbsolutePath()));
        } else if (!result.isDirectory()) {
            throw new IllegalArgumentException(format(ERROR_GIVEN_WORKING_DIRECTORY_FILE_IS_NOT_A_DIRECTORY, result.getAbsolutePath()));
        }
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, result.getAbsolutePath());
        return result;
    }
}
