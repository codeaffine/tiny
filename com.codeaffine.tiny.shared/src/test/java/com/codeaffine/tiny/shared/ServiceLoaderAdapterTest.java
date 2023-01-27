package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceLoaderAdapterTest {

    @Test
    void collectServiceTypeFactories() {
        ServiceLoaderAdapter<ServiceLoaderAdapterTestServiceFactory> loaderAdapter = new ServiceLoaderAdapter<>(ServiceLoaderAdapterTestServiceFactory.class);

        List<ServiceLoaderAdapterTestServiceFactory> actual = loaderAdapter.collectServiceTypeFactories();

        assertThat(actual)
            .hasSize(1)
            .allSatisfy(factory -> assertThat(factory.getClass()).isSameAs(ServiceLoaderAdapterTestServiceFactoryImpl.class))
            .allSatisfy(factory -> assertThat(factory.create()).isNotNull());
    }

    @Test
    void collectServiceTypeFactoryClassNames() {
        ServiceLoaderAdapter<ServiceLoaderAdapterTestServiceFactory> loaderAdapter = new ServiceLoaderAdapter<>(ServiceLoaderAdapterTestServiceFactory.class);

        String actual = loaderAdapter.collectServiceTypeFactoryClassNames();

        assertThat(actual).isEqualTo(ServiceLoaderAdapterTestServiceFactoryImpl.class.getName());
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new ServiceLoaderAdapter<>(null))
            .isInstanceOf(NullPointerException.class);
    }
}
