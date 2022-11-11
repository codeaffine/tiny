package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
class FilesTestHelper {

    static File fakeFileThatCannotBeDeleted() {
        return new File("unknown") {
            @Override
            public boolean exists() {
                return true;
            }
        };
    }
}
