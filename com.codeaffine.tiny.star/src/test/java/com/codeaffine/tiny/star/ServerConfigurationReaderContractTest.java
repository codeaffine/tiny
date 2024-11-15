/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.CLASSPATH;
import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.FILESYSTEM;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

interface ServerConfigurationReaderContractTest {

    String ATTRIBUTE_NAME = "attribute";
    String ATTRIBUTE_VALUE = "value";
    String ATTRIBUTE_DEFAULT_VALUE = "defaultValue";
    String INVALID_SERIALISATION_FORMAT = "invalid";
    String STORE_PASSWORD = "store-password";
    String KEY_PASSWORD = "key-password";
    String KEY_ALIAS = "tiny";
    String KEY_STORE_FILE_ON_CLASSPATH = SecureSocketLayerConfiguration.class.getPackage().getName().replaceAll("\\.", "/") + "/tiny.jks";
    String KEY_STORE_FILE_ON_FILE_SYSTEM = "src/test/resources/" + KEY_STORE_FILE_ON_CLASSPATH;

    ServerConfigurationReader newServerConfigurationReader(String serialized);
    String getConfigurationJson(KeyStoreLocationType keyStoreLocationType, String keyStoreFile);

    @Test
    default void readEnvironmentConfigurationAttribute() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeIfAttributeNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsDefaultValueArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", null, String.class);

        assertThat(actual).isNull();
    }

    @Test
    default void readEnvironmentConfigurationAttributeIfEnvironmentVariableIsNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(null);

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String.class);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeUsingFactoryArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        String actual = configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfAttributeNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeUsingFactoryArgumentIfEnvironmentVariableIsNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        String actual = configurationReader.readEnvironmentConfigurationAttribute("unknown", ATTRIBUTE_DEFAULT_VALUE, String::valueOf);

        assertThat(actual).isEqualTo(ATTRIBUTE_DEFAULT_VALUE);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsAttributeArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsAttributeArgumentUsingFactory() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(null, ATTRIBUTE_DEFAULT_VALUE, String::valueOf))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsFactoryArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(
            ATTRIBUTE_NAME,
            ATTRIBUTE_DEFAULT_VALUE,
            (Function<String, String>) null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readEnvironmentConfigurationAttributeWithNullAsTypeArgument() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        assertThatThrownBy(() -> configurationReader.readEnvironmentConfigurationAttribute(ATTRIBUTE_NAME, ATTRIBUTE_DEFAULT_VALUE, (Class<String>) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    default void readSecureSocketLayerConfigurationFromClasspath() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(CLASSPATH, KEY_STORE_FILE_ON_CLASSPATH));

        SecureSocketLayerConfiguration actual = configurationReader.readSecureSocketLayerConfiguration();

        assertThat(actual.getKeyStore()).isNotNull();
        assertThat(actual.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
        assertThat(actual.getKeyAlias()).isEqualTo(KEY_ALIAS);
        assertThat(actual.getKeyPassword()).isEqualTo(KEY_PASSWORD);
    }

    @Test
    default void readSecureSocketLayerConfigurationFromFileSystem() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(FILESYSTEM, KEY_STORE_FILE_ON_FILE_SYSTEM));

        SecureSocketLayerConfiguration actual = configurationReader.readSecureSocketLayerConfiguration();

        assertThat(actual.getKeyStore()).isNotNull();
        assertThat(actual.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
        assertThat(actual.getKeyAlias()).isEqualTo(KEY_ALIAS);
        assertThat(actual.getKeyPassword()).isEqualTo(KEY_PASSWORD);
    }

    @Test
    default void readSecureSocketLayerConfigurationIfNotSet() {
        ServerConfigurationReader configurationReader = newServerConfigurationReader(getConfigurationJson(null, null));

        SecureSocketLayerConfiguration actual = configurationReader.readSecureSocketLayerConfiguration();

        assertThat(actual).isNull();
    }

    default String createConfigurationJson(KeyStoreLocationType keyStoreLocationType, String keyStoreFile) {
        if(isNull(keyStoreFile) && isNull(keyStoreLocationType)) {
            return format("{\"%s\":\"%s\"}", ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
        }
        return format("{\"%s\":\"%s\", \"%s\":{\"%s\":\"%s:%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}}",
            ATTRIBUTE_NAME,
            ATTRIBUTE_VALUE,
            CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER,
            CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION,
            keyStoreLocationType.name().toLowerCase(),
            keyStoreFile,
            CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_PASSWORD,
            STORE_PASSWORD,
            CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_ALIAS,
            KEY_ALIAS,
            CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_PASSWORD,
            KEY_PASSWORD);
    }
}
