/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceLoaderAdapterTest {

    private static final Class<ServiceLoaderAdapterTestService> SERVICE_CLASS = ServiceLoaderAdapterTestService.class;

    @Test
    void collectServiceTypeImplementations() {
        ServiceLoaderAdapter<ServiceLoaderAdapterTestService> loaderAdapter = new ServiceLoaderAdapter<>(SERVICE_CLASS, ServiceLoader::load);

        List<ServiceLoaderAdapterTestService> actual = loaderAdapter.collectServiceTypeImplementations();

        assertThat(actual)
            .hasSize(1)
            .allSatisfy(factory -> assertThat(factory.getClass()).isSameAs(ServiceLoaderAdapterTestServiceImpl.class))
            .allSatisfy(factory -> assertThat(factory.create()).isNotNull());
    }

    @Test
    void collectServiceTypeImplementationClassNames() {
        ServiceLoaderAdapter<ServiceLoaderAdapterTestService> loaderAdapter = new ServiceLoaderAdapter<>(SERVICE_CLASS, ServiceLoader::load);

        String actual = loaderAdapter.collectServiceTypeImplementationClassNames();

        assertThat(actual).isEqualTo(ServiceLoaderAdapterTestServiceImpl.class.getName());
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new ServiceLoaderAdapter<>(null, ServiceLoader::load))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsServiceLoaderFunctionArgument() {
        assertThatThrownBy(() -> new ServiceLoaderAdapter<>(SERVICE_CLASS, null))
            .isInstanceOf(NullPointerException.class);
    }
}
