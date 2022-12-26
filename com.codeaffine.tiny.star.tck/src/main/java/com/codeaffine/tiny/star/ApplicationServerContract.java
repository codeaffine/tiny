package com.codeaffine.tiny.star;

import org.eclipse.rap.rwt.application.Application;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static com.codeaffine.tiny.star.common.Threads.sleepFor;

public interface ApplicationServerContract {

    @Test
    default void passLifecycle() {
        ApplicationServer applicationServer = create();

        applicationServer.start();
        sleepFor( 1000 );
        applicationServer.stop();
    }

    default ApplicationServer create() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File workingDirectory = new File(tempDir, getClass().getName() + "-" + UUID.randomUUID());
        if(!workingDirectory.mkdirs()) {
            throw new IllegalStateException("Could not create working directory: " + workingDirectory);
        }
        return newApplicationServerBuilder(ApplicationServerContract::configure)
            .withWorkingDirectory(workingDirectory)
            .build();
    }

    private static void configure(Application application) {
        application.addEntryPoint("/ui", TestEntryPoint.class, null);
    }
}
