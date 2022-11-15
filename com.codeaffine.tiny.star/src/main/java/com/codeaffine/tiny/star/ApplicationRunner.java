package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Files.createTemporayDirectory;
import static com.codeaffine.tiny.star.Files.deleteDirectory;
import static com.codeaffine.tiny.star.ServerConfigurationReader.readEnvironmentConfigurationAttribute;
import static lombok.Builder.Default;
import static org.slf4j.LoggerFactory.getLogger;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import java.util.List;
import lombok.Builder;
import lombok.Singular;

@Builder(
    builderMethodName = "newApplicationRunnerBuilder",
    setterPrefix = "with"
)
public class ApplicationRunner {

    public static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = ServerConfigurationReader.ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION;
    public static final String CONFIGURATION_ATTRIBUTE_HOST = "host";
    public static final String CONFIGURATION_ATTRIBUTE_PORT = "port";
    public static final String CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY = "working-directory";
    public static final String CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = "delete-working-directory-on-shutdown";

    private ApplicationConfiguration applicationConfiguration;
    @Default
    private final String host = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_HOST, "localhost", String.class);
    @Default
    private final int port = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_PORT, 8080, Integer.class);
    @Default
    private final File workingDirectory = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY, null, File::new);
    @Default
    private final boolean deleteWorkingDirectoryOnShutdown
        = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN, true, Boolean.class);
    @Singular
    private List<Runnable> deleteWorkingDirectoryOnProcessShutdownPreprocessors;
    @Singular
    private List<Object> lifecycleListeners;

    public ApplicationInstance run() {
        long beginStartup = currentTimeMillis();
        File applicationWorkingDirectory = prepareWorkingDirectory();
        System.setProperty("com.codeaffine.tiny.star.workingDirectory", applicationWorkingDirectory.getAbsolutePath());
        Server server = new DelegatingServerFactory().create(port, host, applicationWorkingDirectory, applicationConfiguration);
        Logger logger = getLogger(getClass());

        Runnable runnable = () -> terminate(server, applicationWorkingDirectory);
        ApplicationInstanceImpl applicationInstance = new ApplicationInstanceImpl(applicationConfiguration.getClass().getName(), server::start, server::stop);
        lifecycleListeners.forEach(applicationInstance::registerLifecycleListener);

        logger.atInfo().log("Starting {} instance with embedded {}.", applicationConfiguration.getClass().getName(), server.getName());
        logger.atInfo().log("Application working directory: {}", applicationWorkingDirectory.getAbsolutePath());
        applicationInstance.start();
        long endStartup = currentTimeMillis();
        logger.atInfo().log("Starting instance of {} took {} ms.", applicationConfiguration.getClass().getName(), (endStartup - beginStartup));
        return applicationInstance;
    }

    private File prepareWorkingDirectory() {
        File result = workingDirectory;
        if(isNull(workingDirectory)) {
            result = createTemporayDirectory(applicationConfiguration.getClass().getName());
        }
        if(deleteWorkingDirectoryOnShutdown) {
            Runnable shutdown = new DeleteWorkingDirectoryOnProcessShutdownHandler(result, deleteWorkingDirectoryOnProcessShutdownPreprocessors);
            getRuntime().addShutdownHook(new Thread(shutdown));
        }
        return result;
    }

    private void terminate(Server server, File applicationWorkingDirectory) {
        server.stop();
        if (deleteWorkingDirectoryOnShutdown) {
            deleteDirectory(applicationWorkingDirectory);
        }
    }
}
