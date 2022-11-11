package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Files.deleteDirectory;
import static lombok.AccessLevel.PACKAGE;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class ApplicationInstanceImpl implements ApplicationInstance {

    @NonNull
    private final Server server;
    @NonNull
    private final File applicationWorkingDirectory;
    private final boolean deleteWorkingDirectoryOnShutdown;

    @Override
    public void terminate() {
        server.stop();
        if (deleteWorkingDirectoryOnShutdown) {
            deleteDirectory(applicationWorkingDirectory);
        }
    }
}
