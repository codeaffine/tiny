/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.log4j;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class Log4j2LoggingFrameworkControlFactoryTest {

    @Test
    void load() {
        ServiceLoaderAdapter<LoggingFrameworkControlFactory> loader = new ServiceLoaderAdapter<>(LoggingFrameworkControlFactory.class, ServiceLoader::load);

        List<LoggingFrameworkControlFactory> actual = loader.collectServiceTypeFactories();

        assertThat(actual)
            .hasSize(1)
            .allSatisfy(factory -> assertThat(factory.getClass()).isSameAs(Log4j2LoggingFrameworkControlFactory.class))
            .allSatisfy(factory -> assertThat(factory.create()).isInstanceOf(Log4j2LoggingFrameworkControl.class));
    }
}
