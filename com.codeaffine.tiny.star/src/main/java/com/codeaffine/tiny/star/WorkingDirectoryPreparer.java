/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;

import static com.codeaffine.tiny.shared.IoUtils.createTemporayDirectory;
import static com.codeaffine.tiny.star.Texts.ERROR_GIVEN_WORKING_DIRECTORY_DOES_NOT_EXIST;
import static com.codeaffine.tiny.star.Texts.ERROR_GIVEN_WORKING_DIRECTORY_FILE_IS_NOT_A_DIRECTORY;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class WorkingDirectoryPreparer {

    static final String ILLEGAL_FILENAME_CHARACTERS = "[^a-zA-Z0-9.\\-]";

    @NonNull
    private final ApplicationServer applicationServer;

    File prepareWorkingDirectory() {
        File result = applicationServer.workingDirectory;
        if (isNull(applicationServer.workingDirectory)) {
            result = createTemporayDirectory(encode(applicationServer.getIdentifier()));
        } else if (!result.exists()) {
            throw new IllegalArgumentException(format(ERROR_GIVEN_WORKING_DIRECTORY_DOES_NOT_EXIST, result.getAbsolutePath()));
        } else if (!result.isDirectory()) {
            throw new IllegalArgumentException(format(ERROR_GIVEN_WORKING_DIRECTORY_FILE_IS_NOT_A_DIRECTORY, result.getAbsolutePath()));
        }
        System.setProperty(applicationServer.getWorkingDirectorSystemProperty(), result.getAbsolutePath());
        return result;
    }

    private static String encode(String name) {
        return name.replaceAll(ILLEGAL_FILENAME_CHARACTERS, "_");
    }
}
