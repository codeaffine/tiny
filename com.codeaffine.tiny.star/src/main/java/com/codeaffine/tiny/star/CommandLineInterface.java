package com.codeaffine.tiny.star;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class CommandLineInterface {

    private final Logger logger;

    public CommandLineInterface() {
        logger = LoggerFactory.getLogger(CommandLineInterface.class);
    }

    @ApplicationInstance.Started
    void startCli(ApplicationInstance applicationInstance) {
        ExecutorService executor = newSingleThreadExecutor();
        executor.execute(() -> {
            logger.atInfo().log("Press q to stop instance of {}.", applicationInstance.getName());
            try (Scanner scanner = new Scanner(System.in)) {
                String line = null;
                while (!"q".equals(line)) {
                    line = scanner.next();
                }
            }
            long beginShutdown = currentTimeMillis();
            logger.atInfo().log("Stopping instance of {}.", applicationInstance.getName());
            applicationInstance.stop();
            executor.shutdown();
            long endShutdown = currentTimeMillis();
            logger.atInfo().log("{} instance stopped in {} ms.", applicationInstance.getName(), (endShutdown - beginShutdown));
        });
    }
}
