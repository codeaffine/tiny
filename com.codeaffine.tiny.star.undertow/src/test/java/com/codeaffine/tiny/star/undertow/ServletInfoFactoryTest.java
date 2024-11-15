/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import io.undertow.servlet.api.ServletInfo;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServletInfoFactoryTest {

    @Test
    void createRwtServletInfo() {
        ServletInfoFactory factory = new ServletInfoFactory(MULTI_ENTRYPOINT_CONFIGURATION);

        ServletInfo actual = factory.createRwtServletInfo();

        assertThat(actual.getName()).isEqualTo(ServletInfoFactory.SERVLET_NAME);
        assertThat(actual.getLoadOnStartup()).isEqualTo(ServletInfoFactory.LOAD_ON_STARTUP);
        assertThat(HttpServlet.class).isAssignableFrom(actual.getServletClass());
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
