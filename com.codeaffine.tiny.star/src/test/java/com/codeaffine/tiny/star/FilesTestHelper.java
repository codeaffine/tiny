package com.codeaffine.tiny.star;

import lombok.NoArgsConstructor;

import java.io.File;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class FilesTestHelper {

    public static File fakeFileThatCannotBeDeleted() {
        return new File("unknown") {
            @Override
            public boolean exists() {
                return true;
            }
        };
    }
}
