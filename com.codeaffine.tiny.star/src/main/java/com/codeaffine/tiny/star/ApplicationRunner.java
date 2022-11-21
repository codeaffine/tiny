package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ApplicationInstance.State.RUNNING;
import static com.codeaffine.tiny.star.IoUtils.createTemporayDirectory;
import static com.codeaffine.tiny.star.IoUtils.findFreePort;
import static com.codeaffine.tiny.star.Messages.INFO_SERVER_USAGE;
import static com.codeaffine.tiny.star.Messages.INFO_STARTUP_CONFIRMATION;
import static com.codeaffine.tiny.star.Messages.INFO_WORKING_DIRECTORY;
import static com.codeaffine.tiny.star.ServerConfigurationReader.readEnvironmentConfigurationAttribute;
import static com.codeaffine.tiny.star.common.Metric.measureDuration;
import static lombok.Builder.Default;
import static org.slf4j.LoggerFactory.getLogger;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import com.codeaffine.tiny.star.extrinsic.DelegatingLoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.Server;

import java.io.File;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder(
    builderMethodName = "newApplicationRunnerBuilder",
    setterPrefix = "with"
)
public class ApplicationRunner {

    public static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = ServerConfigurationReader.ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION;
    public static final String CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = "delete-working-directory-on-shutdown";
    public static final String CONFIGURATION_ATTRIBUTE_HOST = "host";
    public static final String CONFIGURATION_ATTRIBUTE_PORT = "port";
    public static final String CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY = "working-directory";
    public static final String DEFAULT_HOST = "localhost";
    public static final boolean DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = true;
    public static final String SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY = "com.codeaffine.tiny.star.workingDirectory";

    @NonNull
    private ApplicationConfiguration applicationConfiguration;
    @Default
    private final String host = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_HOST, DEFAULT_HOST, String.class);
    @Default
    private final int port = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_PORT, findFreePort(), Integer.class);
    @Default
    private final File workingDirectory = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY, null, File::new);
    @Default
    private final boolean deleteWorkingDirectoryOnShutdown
        = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                                                DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                                                Boolean.class);
    @Singular
    private List<Object> lifecycleListeners;
    private LoggingFrameworkControl loggingFrameworkControl;
    private Logger logger;

    public ApplicationInstance run() {
        return measureDuration(this::doRun)
            .report((value, duration) -> logger.info(INFO_STARTUP_CONFIRMATION, applicationConfiguration.getClass().getName(), duration));
    }

    private ApplicationInstanceImpl doRun() {
        File applicationWorkingDirectory = prepareWorkingDirectory();
        ClassLoader applicationClassLoader = applicationConfiguration.getClass().getClassLoader();
        loggingFrameworkControl = isNull(loggingFrameworkControl) ? new DelegatingLoggingFrameworkControl(applicationClassLoader) : loggingFrameworkControl;
        loggingFrameworkControl.configure(applicationClassLoader, applicationConfiguration.getClass().getName());
        logger = isNull(logger) ? getLogger(ApplicationRunner.class) : logger;
        Server server = new DelegatingServerFactory().create(port, host, applicationWorkingDirectory, applicationConfiguration);
        Terminator terminator = newTerminator(applicationWorkingDirectory, server, loggingFrameworkControl);
        ApplicationInstanceImpl result = new ApplicationInstanceImpl(applicationConfiguration.getClass().getName(), server::start, terminator);
        getRuntime().addShutdownHook(new Thread(() -> runShutdownHookHandler(terminator, result)));
        lifecycleListeners.forEach(result::registerLifecycleListener);
        logger.info(INFO_SERVER_USAGE, applicationConfiguration.getClass().getName(), server.getName());
        logger.info(INFO_WORKING_DIRECTORY, applicationWorkingDirectory.getAbsolutePath());
        result.start();
        return result;
    }

    private File prepareWorkingDirectory() {
        File result = workingDirectory;
        if(isNull(workingDirectory)) {
            result = createTemporayDirectory(applicationConfiguration.getClass().getName());
        } else if(!result.exists()) {
            throw new IllegalArgumentException(format(Messages.ERROR_GIVEN_WORKING_DIRECTORY_DOES_NOT_EXIST, result.getAbsolutePath()));
        } else if(!result.isDirectory()) {
            throw new IllegalArgumentException(format(Messages.ERROR_GIVEN_WORKING_DIRECTORY_FILE_IS_NOT_A_DIRECTORY, result.getAbsolutePath()));
        }
        System.setProperty(SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY, result.getAbsolutePath());
        return result;
    }

    private Terminator newTerminator(File applicationWorkingDirectory, Server server, @NonNull LoggingFrameworkControl loggingFrameworkControl) {
        return new Terminator(applicationWorkingDirectory, server,  loggingFrameworkControl, deleteWorkingDirectoryOnShutdown); // NOSONAR: false positive, Terminator has methods that are not static
    }

    private static void runShutdownHookHandler(Terminator terminator, ApplicationInstanceImpl result) {
        terminator.setShutdownHookExecution(true);
        if(RUNNING == result.getState()) {
            result.stop();
        } else {
            terminator.deleteWorkingDirectory();
        }
    }
}
