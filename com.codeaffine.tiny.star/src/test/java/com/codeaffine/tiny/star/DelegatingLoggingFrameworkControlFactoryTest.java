/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.shared.ServiceLoaderAdapter;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory;
import org.junit.jupiter.api.Test;

import static com.codeaffine.tiny.shared.ServiceLoaderAdapterTestHelper.fakeServiceLoaderAdapter;
import static org.assertj.core.api.Assertions.*;

class DelegatingLoggingFrameworkControlFactoryTest {

    @Test
    void create() {
        DelegatingLoggingFrameworkControlFactory factory = new DelegatingLoggingFrameworkControlFactory();

        LoggingFrameworkControl actual = factory.create();

        assertThat(actual)
            .isNotNull()
            .isInstanceOf(LoggingFrameworkControlFactoryTestFactory.DummyLoggingFrameworkControl.class);
    }

    @Test
    void createIfNoFactoryIsAvailableOnClasspath() {
        ServiceLoaderAdapter<LoggingFrameworkControlFactory> serviceLoaderAdapter = fakeServiceLoaderAdapter();
        DelegatingLoggingFrameworkControlFactory factory = new DelegatingLoggingFrameworkControlFactory(serviceLoaderAdapter);

        LoggingFrameworkControl actual = factory.create();

        assertThat(actual)
            .isNotNull()
            .isInstanceOf(DelegatingLoggingFrameworkControlFactory.DummyLoggingFrameworkControl.class);
    }

    @Test
    void createIfMultiplyFactoriesAreAvailableOnClasspath() {
        LoggingFrameworkControlFactory factory1 = () -> new LoggingFrameworkControl() {};
        LoggingFrameworkControlFactory factory2 = () -> new LoggingFrameworkControl() {};
        ServiceLoaderAdapter<LoggingFrameworkControlFactory> serviceLoaderAdapter = fakeServiceLoaderAdapter(factory1, factory2);
        DelegatingLoggingFrameworkControlFactory factory = new DelegatingLoggingFrameworkControlFactory(serviceLoaderAdapter);

        Throwable actual = catchException(factory::create);

        assertThat(actual)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(factory1.getClass().getName())
            .hasMessageContaining(factory2.getClass().getName());
    }

    @Test
    void constructWithNullAsServiceLoaderAdapterArgument() {
        assertThatThrownBy(() -> new DelegatingLoggingFrameworkControlFactory(null))
            .isInstanceOf(NullPointerException.class);
    }
}
