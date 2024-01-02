/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.io.InputStream;
import java.util.function.Function;

import static com.codeaffine.tiny.star.ApplicationServer.CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION;
import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.CLASSPATH;
import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.FILESYSTEM;
import static com.codeaffine.tiny.star.ServerConfigurationReaderContractTest.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeyStoreLoaderTest {

    public static final String UNKNOWN_KEY_STORE_LOCATION_TYPE = "unknownKeyStoreLocationType";
    public static final String UNKNOWN_KEY_STORE_FILE = "unknown.jks";

    @Test
    void createSecureSocketLayerConfigurationWithKeyStoreOnClasspath() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactory();

        SecureSocketLayerConfiguration actual = KeyStoreLoader.createSecureSocketLayerConfiguration(
            CLASSPATH.name() + KeyStoreLoader.KEY_STORE_LOCATION_SEGMENT_SEPARATOR + KEY_STORE_FILE_ON_CLASSPATH,
            applicationConfiguration,
            factory);

        assertThat(actual).isNotNull();
    }

    @Test
    void createSecureSocketLayerConfigurationWithKeyStoreOnFileSystem() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactory();

        SecureSocketLayerConfiguration actual = KeyStoreLoader.createSecureSocketLayerConfiguration(
            FILESYSTEM.name() + KeyStoreLoader.KEY_STORE_LOCATION_SEGMENT_SEPARATOR + KEY_STORE_FILE_ON_FILE_SYSTEM,
            applicationConfiguration,
            factory);

        assertThat(actual).isNotNull();
    }

    @Test
    void createSecureSocketLayerConfigurationWithUnknownKeyStoreLocationPrefix() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactory();

        Throwable actual = catchThrowable(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(
            UNKNOWN_KEY_STORE_LOCATION_TYPE + KeyStoreLoader.KEY_STORE_LOCATION_SEGMENT_SEPARATOR + KEY_STORE_FILE_ON_FILE_SYSTEM,
            applicationConfiguration,
            factory));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(UNKNOWN_KEY_STORE_LOCATION_TYPE, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION);
    }

    @Test
    void createSecureSocketLayerConfigurationWithKeyStoreOnClasspathAndKeyStoreFileNotFound() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactory();

        Throwable actual = catchThrowable(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(
            CLASSPATH.name() + KeyStoreLoader.KEY_STORE_LOCATION_SEGMENT_SEPARATOR + UNKNOWN_KEY_STORE_FILE,
            applicationConfiguration,
            factory));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(UNKNOWN_KEY_STORE_FILE, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION);
    }

    @Test
    void createSecureSocketLayerConfigurationWithKeyStoreOnFileSystemAndKeyStoreFileNotFound() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactory();

        Throwable actual = catchThrowable(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(
            FILESYSTEM.name() + KeyStoreLoader.KEY_STORE_LOCATION_SEGMENT_SEPARATOR + UNKNOWN_KEY_STORE_FILE,
            applicationConfiguration,
            factory));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(UNKNOWN_KEY_STORE_FILE, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION);
    }

    @Test
    void createSecureSocketLayerConfigurationWithProblemOnFactoryCall() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        IllegalArgumentException expected = new IllegalArgumentException("bad");
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactoryWithProblem(expected);

        Throwable actual = catchThrowable(() ->KeyStoreLoader.createSecureSocketLayerConfiguration(
            CLASSPATH.name() + KeyStoreLoader.KEY_STORE_LOCATION_SEGMENT_SEPARATOR + KEY_STORE_FILE_ON_CLASSPATH,
            applicationConfiguration,
            factory));

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void createSecureSocketLayerConfigurationWithInvalidKeyStoreLocationFormat() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        Function<InputStream, SecureSocketLayerConfiguration> factory = stubSecureSocketLayerFactory();

        Throwable actual = catchThrowable(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(
            KEY_STORE_FILE_ON_CLASSPATH,
            applicationConfiguration,
            factory));

        assertThat(actual)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(Texts.ERROR_WRONG_KEY_STORE_FILE_FORMAT, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createSecureSocketLayerConfigurationWithNullAsKeyStoreLocationArgument() {
        assertThatThrownBy(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(null, mock(ApplicationConfiguration.class), mock(Function.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createSecureSocketLayerConfigurationWithNullAsApplicationConfigurationArgument() {
        assertThatThrownBy(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(KEY_STORE_FILE_ON_CLASSPATH, null, mock(Function.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createSecureSocketLayerConfigurationWithNullAsFactoryArgument() {
        assertThatThrownBy(() -> KeyStoreLoader.createSecureSocketLayerConfiguration(KEY_STORE_FILE_ON_FILE_SYSTEM, mock(ApplicationConfiguration.class), null))
            .isInstanceOf(NullPointerException.class);
    }

    @SuppressWarnings("unchecked")
    private static Function<InputStream, SecureSocketLayerConfiguration> stubSecureSocketLayerFactory() {
        Function<InputStream, SecureSocketLayerConfiguration> result = mock(Function.class);
        when(result.apply(any()))
            .thenAnswer(KeyStoreLoaderTest::createSecureSocketLayerConfiguration);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Function<InputStream, SecureSocketLayerConfiguration> stubSecureSocketLayerFactoryWithProblem(Throwable problem) {
        Function<InputStream, SecureSocketLayerConfiguration> result = mock(Function.class);
        when(result.apply(any())).thenThrow(problem);
        return result;
    }

    private static SecureSocketLayerConfiguration createSecureSocketLayerConfiguration(InvocationOnMock invocation) {
        InputStream inputStream = invocation.getArgument(0);
        return new SecureSocketLayerConfiguration(inputStream, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD);
    }
}
