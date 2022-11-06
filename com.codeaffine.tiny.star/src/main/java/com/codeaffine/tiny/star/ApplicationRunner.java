package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Files.createTemporayDirectory;
import static com.codeaffine.tiny.star.ServerConfigurationReader.readEnvironmentConfigurationAttribute;
import static lombok.Builder.*;
import static org.slf4j.LoggerFactory.getLogger;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Builder;

@Builder(
    builderMethodName = "newApplicationRunnerBuilder",
    setterPrefix = "with"
)
public class ApplicationRunner {

    public static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = ServerConfigurationReader.ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION;

    private ApplicationConfiguration applicationConfiguration;
    @Default
    private final String host = readEnvironmentConfigurationAttribute("host", "localhost", String.class);
    @Default
    private final File workingDirectory = readEnvironmentConfigurationAttribute("working-directory", null, File::new);
    @Default
    private final int port = readEnvironmentConfigurationAttribute("port", 8080, Integer.class);

    public void run() {
        long beginStartup = currentTimeMillis();
        File serverWorkingDirectory = ensureWorkingDirectory(workingDirectory, applicationConfiguration);
        System.setProperty("com.codeaffine.tiny.star.workingDirectory", serverWorkingDirectory.getAbsolutePath());
        Server server = new DelegatingServerFactory().create(port, host, serverWorkingDirectory, applicationConfiguration);
        Logger logger = getLogger(getClass());
        logger.atInfo().log("starting {} on embedded {}.", applicationConfiguration.getClass().getName(), server.getName());
        logger.atInfo().log("using server working directory: {}", serverWorkingDirectory.getAbsolutePath());

        ExecutorService executor = newSingleThreadExecutor();
        executor.execute(() -> {
            try(Scanner scanner = new Scanner(System.in)) {
                String line = null;
                while (!"q".equals(line)) {
                    line = scanner.next();
                }
            }
            long beginShutdown = currentTimeMillis();
            logger.atInfo().log("stopping {}.", applicationConfiguration.getClass().getName());
            server.stop();
            executor.shutdown();
            long endShutdown = currentTimeMillis();
            logger.atInfo().log("{} stopped in {} ms.", applicationConfiguration.getClass().getName(), (endShutdown - beginShutdown));
        });

        server.start();
        long endStartup = currentTimeMillis();
        logger.atInfo().log("starting {} took {} ms.", applicationConfiguration.getClass().getName(), (endStartup - beginStartup));
    }

    private static File ensureWorkingDirectory(File workingDirectory, ApplicationConfiguration applicationConfiguration) {
        if(isNull(workingDirectory)) {
            return createTemporayDirectory(applicationConfiguration.getClass().getName());
        }
        return workingDirectory;
    }
}
