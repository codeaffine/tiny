/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.KeyStoreType;
import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.KeyStoreType.JKS;
import static com.codeaffine.tiny.star.spi.Texts.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecureSocketLayerConfigurationTest implements ArgumentConverter {

    private static final Supplier<InputStream> JKS_KEY_STORE = () -> SecureSocketLayerConfigurationTest.class.getResourceAsStream("tiny.jks");
    private static final Supplier<InputStream> PKCS12_KEY_STORE = () -> SecureSocketLayerConfigurationTest.class.getResourceAsStream("tiny.p12");
    private static final String KEY_STORE_PASSWORD = "store-password"; // valid password for both key stores
    private static final String KEY_PASSWORD = "key-password"; // valid password for JKS key store only
    private static final String ALIAS = "tiny"; // valid alias for JKS key store only
    private static final String UNKNOWN_ALIAS = "unknownAlias";

    @Test
    void construct() throws IOException {
        InputStream keyStore = JKS_KEY_STORE.get();
        byte[] bytes = keyStore.readAllBytes();

        SecureSocketLayerConfiguration configuration
            = new SecureSocketLayerConfiguration(new ByteArrayInputStream(bytes), KEY_STORE_PASSWORD, ALIAS, KEY_PASSWORD);

        assertThat(configuration.getKeyStoreType()).isSameAs(JKS);
        assertThat(configuration.getKeyStorePassword()).isEqualTo(KEY_STORE_PASSWORD);
        assertThat(configuration.getKeyAlias()).isEqualTo(ALIAS);
        assertThat(configuration.getKeyPassword()).isEqualTo(KEY_PASSWORD);
        assertThat(configuration.getKeyStore().readAllBytes())
            .isEqualTo(configuration.getKeyStore().readAllBytes())
            .isEqualTo(bytes);
    }

    @ParameterizedTest
    @CsvSource({
        "JKS_KEY_STORE, store-password, tiny, key-password, key-password",
        "PKCS12_KEY_STORE, store-password, , , store-password",
    })
    void getKeyPassword(
        @ConvertWith(SecureSocketLayerConfigurationTest.class) InputStream keyStore,
        String keyStorePassword,
        String alias,
        String keyPassword,
        String expected)
    {
        SecureSocketLayerConfiguration configuration = new SecureSocketLayerConfiguration(keyStore, keyStorePassword, alias, keyPassword);

        String actual = configuration.getKeyPassword();

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "JKS_KEY_STORE, store-password, key-password, JKS",
        "PKCS12_KEY_STORE, store-password, store-password, PKCS12",
    })
    void getKeyStoreType(
        @ConvertWith(SecureSocketLayerConfigurationTest.class) InputStream keyStore,
        String keyStorePassword,
        String keyPassword,
        KeyStoreType expected)
    {
        SecureSocketLayerConfiguration configuration = new SecureSocketLayerConfiguration(keyStore, keyStorePassword, ALIAS, keyPassword);

        KeyStoreType actual = configuration.getKeyStoreType();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void constructWithInvalidKeyStorePasswordArgument() {
        InputStream keyStore = JKS_KEY_STORE.get();

        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(keyStore, "invalid", ALIAS, KEY_PASSWORD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_INVALID_KEY_STORE_PASSWORD);
    }

    @Test
    void constructWithInvalidKeyPasswordArgument() {
        InputStream keyStore = JKS_KEY_STORE.get();

        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, ALIAS, "invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_INVALID_KEY_PASSWORD);
    }

    @Test
    void constructWithUnknownAliasArgument() {
        InputStream keyStore = JKS_KEY_STORE.get();

        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, UNKNOWN_ALIAS, KEY_PASSWORD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(UNKNOWN_ALIAS);
    }

    @Test
    void constructWithUnknownKeyStoreTypeAsKeyStoreArgument() {
        InputStream keyStore = new ByteArrayInputStream(invalidKeyStoreContent());

        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, ALIAS, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_UNABLE_TO_DETECT_KEYSTORE_TYPE);
    }

    @Test
    void constructWithEmptyInputStreamAsKeyStoreArgument() {
        InputStream keyStore = new ByteArrayInputStream(new byte[0]);

        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, ALIAS, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_INVALID_KEY_STORE);
    }

    @Test
    void constructWithBrokenInputStreamAsKeyStoreArgument() throws IOException {
        IOException expectedCause = new IOException();
        InputStream keyStore = mock(InputStream.class);
        when(keyStore.readAllBytes()).thenThrow(expectedCause);

        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(keyStore, KEY_STORE_PASSWORD, ALIAS, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ERROR_READING_KEY_STORE)
            .hasCause(expectedCause);
    }

    @Test
    void constructWithNullAsKeyStoreArgument() {
        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(null,"store-password", ALIAS, KEY_PASSWORD))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsKeyStorePasswordArgument() {
        assertThatThrownBy(() -> new SecureSocketLayerConfiguration(mock(InputStream.class),null, ALIAS, KEY_PASSWORD))
            .isInstanceOf(NullPointerException.class);
    }

    private static byte[] invalidKeyStoreContent() {
        return ByteBuffer
            .allocate(4)
            .putInt(8)
            .array();
    }

    @Override // convert Key Store labels to actual Key Store input streams
    public Object convert(Object o, ParameterContext parameterContext) throws ArgumentConversionException {
        String keyStore = (String) o;
        return switch (keyStore) {
            case "JKS_KEY_STORE" -> JKS_KEY_STORE.get();
            case "PKCS12_KEY_STORE" -> PKCS12_KEY_STORE.get();
            default -> throw new IllegalArgumentException("Unknown key store definition: " + keyStore);
        };
    }
}
