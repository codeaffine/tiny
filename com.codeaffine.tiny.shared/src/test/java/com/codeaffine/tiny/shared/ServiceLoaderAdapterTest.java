package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceLoaderAdapterTest {

    private static final Class<ServiceLoaderAdapterTestServiceFactory> SERVICE_FACTORY_CLASS = ServiceLoaderAdapterTestServiceFactory.class;

    @Test
    void collectServiceTypeFactories() {
        ServiceLoaderAdapter<ServiceLoaderAdapterTestServiceFactory> loaderAdapter = new ServiceLoaderAdapter<>(SERVICE_FACTORY_CLASS, ServiceLoader::load);

        List<ServiceLoaderAdapterTestServiceFactory> actual = loaderAdapter.collectServiceTypeFactories();

        assertThat(actual)
            .hasSize(1)
            .allSatisfy(factory -> assertThat(factory.getClass()).isSameAs(ServiceLoaderAdapterTestServiceFactoryImpl.class))
            .allSatisfy(factory -> assertThat(factory.create()).isNotNull());
    }

    @Test
    void collectServiceTypeFactoryClassNames() {
        ServiceLoaderAdapter<ServiceLoaderAdapterTestServiceFactory> loaderAdapter = new ServiceLoaderAdapter<>(SERVICE_FACTORY_CLASS, ServiceLoader::load);

        String actual = loaderAdapter.collectServiceTypeFactoryClassNames();

        assertThat(actual).isEqualTo(ServiceLoaderAdapterTestServiceFactoryImpl.class.getName());
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new ServiceLoaderAdapter<>(null, ServiceLoader::load))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsServiceLoaderFunctionArgument() {
        assertThatThrownBy(() -> new ServiceLoaderAdapter<>(SERVICE_FACTORY_CLASS, null))
            .isInstanceOf(NullPointerException.class);
    }
}
