package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.servlet.RwtServletAdapter;
import io.undertow.servlet.api.ServletInfo;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.ApplicationServerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServletInfoFactoryTest {

    @Test
    void createRwtServletInfo() {
        ServletInfoFactory factory = new ServletInfoFactory(MULTI_ENTRYPOINT_CONFIGURATION);

        ServletInfo actual = factory.createRwtServletInfo();

        assertThat(actual.getName()).isEqualTo(ServletInfoFactory.SERVLET_NAME);
        assertThat(actual.getServletClass()).isEqualTo(RwtServletAdapter.class);
        assertThat(actual.getMappings()).containsExactlyInAnyOrder(toPattern(ENTRYPOINT_PATH_1), toPattern(ENTRYPOINT_PATH_2));
    }

    @Test
    void constructWithNullAsApplicationConfigurationArgument() {
        assertThatThrownBy(() -> new ServletInfoFactory(null))
            .isInstanceOf(NullPointerException.class);
    }

    private static String toPattern(String entrypointPath) {
        return entrypointPath + ServletInfoFactory.ALL_SUB_PATHS_PATTERN;
    }
}
