package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.ShutdownHookHandler.beforeProcessShutdown;
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
import java.util.function.Supplier;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder(
    builderMethodName = "newDefaultApplicationRunnerBuilder",
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

    private static final String ILLEGAL_FILENAME_CHARACTERS = "[^a-zA-Z0-9.\\-]";

    @NonNull
    private final ApplicationConfiguration applicationConfiguration;
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
    private String applicationIdentifier;

    public static ApplicationRunnerBuilder newApplicationRunnerBuilder(@NonNull ApplicationConfiguration applicationConfiguration) {
        return newDefaultApplicationRunnerBuilder()
            .withApplicationConfiguration(applicationConfiguration);
    }

    public ApplicationInstance run() {
        return runInternal(getLogger(getClass()));
    }

    ApplicationInstance runInternal(Logger logger) {
        applicationIdentifier = isNull(applicationIdentifier) ? encode(applicationConfiguration.getClass().getName()) : applicationIdentifier;
        return measureDuration(() -> doRun(() -> ensureLogger(logger)))
            .report((value, duration) -> logger.info(INFO_STARTUP_CONFIRMATION, applicationIdentifier, duration));
    }

    private ApplicationInstanceImpl doRun(Supplier<Logger> loggerSupplier) {
        Logger logger = loggerSupplier.get();
        File applicationWorkingDirectory = prepareWorkingDirectory();
        ClassLoader applicationClassLoader = applicationConfiguration.getClass().getClassLoader();
        loggingFrameworkControl = isNull(loggingFrameworkControl) ? new DelegatingLoggingFrameworkControl(applicationClassLoader) : loggingFrameworkControl;
        loggingFrameworkControl.configure(applicationClassLoader, applicationIdentifier);
        Server server = new DelegatingServerFactory().create(port, host, applicationWorkingDirectory, applicationConfiguration);
        Terminator terminator = newTerminator(applicationWorkingDirectory, server, loggingFrameworkControl);
        ApplicationInstanceImpl result = new ApplicationInstanceImpl(applicationIdentifier, server::start, terminator);
        getRuntime().addShutdownHook(new Thread(() -> beforeProcessShutdown(terminator, result)));
        lifecycleListeners.forEach(result::registerLifecycleListener);
        logger.info(INFO_SERVER_USAGE, applicationIdentifier, server.getName());
        logger.info(INFO_WORKING_DIRECTORY, applicationWorkingDirectory.getAbsolutePath());
        result.start();
        return result;
    }

    private File prepareWorkingDirectory() {
        File result = workingDirectory;
        if(isNull(workingDirectory)) {
            result = createTemporayDirectory(applicationIdentifier);
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

    private static String encode(String name) {
        return name.replaceAll(ILLEGAL_FILENAME_CHARACTERS, "_");
    }

    private static Logger ensureLogger(Logger logger) {
        return isNull(logger) ? getLogger(ApplicationRunner.class) : logger;
    }
}
