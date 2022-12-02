package com.codeaffine.tiny.star;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Set;

class EntrypointPathCaptorTest {

    private static final String PATH_1 = "path1";
    private static final String PATH_2 = "path2";
    private static final String PATH_3 = "path3";
    private static final String PATH_4 = "path4";

    @Test
    void captureEntrypointPaths() {
        ApplicationConfiguration applicationConfiguration = application -> {
            application.addEntryPoint(PATH_1, AbstractEntryPoint.class, null);
            application.addEntryPoint(PATH_3, () -> null, null);
            application.addEntryPoint(PATH_2, AbstractEntryPoint.class, null);
            application.addEntryPoint(PATH_4, () -> null, null);
        };

        Set<String> actual = EntrypointPathCaptor.captureEntrypointPaths(applicationConfiguration);

        assertThat(actual).containsExactlyInAnyOrder(PATH_1, PATH_2, PATH_3, PATH_4);
    }

    @Test
    void captureEntrypointPathsWithConfigurationThatProvidesNoEntrypoint() {
        ApplicationConfiguration applicationConfiguration = application -> {};

        Set<String> actual = EntrypointPathCaptor.captureEntrypointPaths(applicationConfiguration);

        assertThat(actual).isEmpty();
    }

    @Test
    void captureEntrypointPathsWithNullAsApplicationConfigurationArgument() {
        assertThatThrownBy(() -> EntrypointPathCaptor.captureEntrypointPaths(null))
            .isInstanceOf(NullPointerException.class);
    }

}
