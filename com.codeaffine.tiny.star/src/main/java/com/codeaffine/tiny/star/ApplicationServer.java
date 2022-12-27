package com.codeaffine.tiny.star;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.common.IoUtils.findFreePort;
import static com.codeaffine.tiny.star.ServerConfigurationReader.readEnvironmentConfigurationAttribute;
import static com.codeaffine.tiny.star.Texts.*;
import static com.codeaffine.tiny.star.common.Metric.measureDuration;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.time.LocalDate.now;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.Builder.Default;
import static org.slf4j.LoggerFactory.getLogger;

@Builder(
    builderMethodName = "newDefaultApplicationServerBuilder",
    setterPrefix = "with"
)
public class ApplicationServer {

    public static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = ServerConfigurationReader.ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION;
    public static final String CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = "delete-working-directory-on-shutdown";
    public static final String CONFIGURATION_ATTRIBUTE_HOST = "host";
    public static final String CONFIGURATION_ATTRIBUTE_PORT = "port";
    public static final String CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY = "working-directory";
    public static final String CONFIGURATION_ATTRIBUTE_SHOW_START_INFO = "show-start-info";
    public static final String DEFAULT_HOST = "localhost";
    public static final boolean DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN = true;
    public static final String SYSTEM_PROPERTY_APPLICATION_WORKING_DIRECTORY = "com.codeaffine.tiny.star.workingDirectory";

    private static final String ILLEGAL_FILENAME_CHARACTERS = "[^a-zA-Z0-9.\\-]";

    @NonNull
    final ApplicationConfiguration applicationConfiguration;
    @Default
    final String host = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_HOST, DEFAULT_HOST, String.class);
    @Default
    final int port = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_PORT, findFreePort(), Integer.class);
    @Default
    final File workingDirectory = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_WORKING_DIRECTORY, null, File::new);
    @Default
    final boolean deleteWorkingDirectoryOnShutdown
        = readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                                                DEFAULT_DELETE_WORKING_DIRECTORY_ON_SHUTDOWN,
                                                Boolean.class);
    @Default
    final Function<ApplicationServer, String> startInfoProvider
        =   TRUE.equals(readEnvironmentConfigurationAttribute(CONFIGURATION_ATTRIBUTE_SHOW_START_INFO, TRUE, Boolean.class))
          ? applicationServer -> format(TINY_STAR_START_INFO, applicationServer.getIdentifier(), now().getYear())
          : null;
    @Singular
    final List<Object> lifecycleListeners;
    final String applicationIdentifier;
    final LoggingFrameworkControl loggingFrameworkControl;

    private final AtomicReference<ApplicationProcess> processHolder = new AtomicReference<>();

    public enum State { STARTING, RUNNING, STOPPING, HALTED }

    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Starting {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Started {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Stopping {}
    @Target({ METHOD })
    @Retention(RUNTIME)
    public @interface Stopped {}

    public static ApplicationServerBuilder newApplicationServerBuilder(@NonNull ApplicationConfiguration applicationConfiguration) {
        return newDefaultApplicationServerBuilder()
            .withApplicationConfiguration(applicationConfiguration);
    }

    public String getIdentifier() {
        return isNull(applicationIdentifier) ? encode(applicationConfiguration.getClass().getName()) : applicationIdentifier;
    }

    public State getState() {
        ApplicationProcess applicationProcess = processHolder.get();
        return isNull(applicationProcess) ? HALTED :  applicationProcess.getState();
    }

    public ApplicationServer start() {
        return startInternal(new ApplicationProcessFactory(this), getLogger(getClass()));
    }

    public ApplicationServer stop() {
        return stopInternal(getLogger(getClass()));
    }

    ApplicationServer stopInternal(Logger logger) {
        ApplicationProcess process = processHolder.getAndUpdate(currentProcess -> null);
        if (nonNull(process)) {
            measureDuration(process::stop)
                .report(duration -> logger.info(INFO_SHUTDOWN_CONFIRMATION, getIdentifier(), duration));
        }
        return this;
    }

    ApplicationServer startInternal(ApplicationProcessFactory applicationProcessFactory, Logger logger) {
        ApplicationProcess process = processHolder.updateAndGet(currentProcess -> createProcess(logger, currentProcess, applicationProcessFactory));
        if (process.getState().equals(HALTED)) {
            measureDuration(process::start)
                .report(duration -> logger.info(INFO_STARTUP_CONFIRMATION, getIdentifier(), duration));
        }
        return this;
    }

    private ApplicationProcess createProcess(Logger logger, ApplicationProcess currentProcess, ApplicationProcessFactory applicationProcessFactory) {
        if (nonNull(currentProcess)) {
            return currentProcess;
        }
        return measureDuration(applicationProcessFactory::createProcess)
            .report((value, duration) -> logger.info(INFO_CREATION_CONFIRMATION, getIdentifier(), duration));
    }

    private static String encode(String name) {
        return name.replaceAll(ILLEGAL_FILENAME_CHARACTERS, "_");
    }
}
