/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.codeaffine.tiny.star.ApplicationServer.*;
import static com.codeaffine.tiny.star.Texts.ERROR_READING_ATTRIBUTE;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

class SingleServerConfigurationReader implements ServerConfigurationReader {

    private static final Supplier<String> CONFIGURATION_READER = () -> System.getenv(ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ApplicationConfiguration applicationConfiguration;
    private final Supplier<String> configurationLoader;

    private Map<String, ?> attributeMap;

    SingleServerConfigurationReader(ApplicationConfiguration applicationConfiguration) {
        this(CONFIGURATION_READER, applicationConfiguration);
    }

    SingleServerConfigurationReader(@NonNull Supplier<String> configurationLoader, @NonNull ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
        this.configurationLoader = configurationLoader;
    }

    SingleServerConfigurationReader(@NonNull Map<String, ?> attributeMap, @NonNull ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
        this.configurationLoader = () -> "{}";
        this.attributeMap = attributeMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Class<T> type) {
        tryAttributeMapInitialization(attributeName);
        if(isNull(attributeMap)) {
            return defaultValue;
        }
        return type.cast(((Map<String, T>) attributeMap).getOrDefault(attributeName, defaultValue));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Function<String, T> factory) {
        tryAttributeMapInitialization(attributeName);
        if(isNull(attributeMap)) {
            return defaultValue;
        }
        return readWithFactory(attributeName, defaultValue, factory, (Map<String, String>) attributeMap);
    }

    @Override
    public SecureSocketLayerConfiguration readSecureSocketLayerConfiguration() {
        tryAttributeMapInitialization(CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER);
        if (isNull(attributeMap)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> sslConfigMap = (Map<String, String>) attributeMap.get(CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER);
        if (isNull(sslConfigMap)) {
            return null;
        }
        return KeyStoreLoader.createSecureSocketLayerConfiguration(
            sslConfigMap.get(CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION),
            applicationConfiguration,
            inputStream -> new SecureSocketLayerConfiguration(
                inputStream,
                sslConfigMap.get(CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_PASSWORD),
                sslConfigMap.get(CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_ALIAS),
                sslConfigMap.get(CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEY_PASSWORD))
        );
    }

    private void tryAttributeMapInitialization(String attributeName) {
        if(isNull(attributeMap)) {
            String serialized = readSerializedConfigurationFromEnvironment();
            if(nonNull(serialized)) {
                attributeMap = deserialize(attributeName, serialized);
            }
        }
    }

    private static <T> T readWithFactory(String attributeName, T defaultValue, Function<String, T> factory, Map<String, String> map) {
        String value = map.get(attributeName);
        if (nonNull(value)) {
            return factory.apply(value);
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static <V> Map<String, V> deserialize(String attributeName, String serialized) throws IllegalArgumentException {
        try {
            return OBJECT_MAPPER.readValue(serialized, Map.class);
        } catch (JsonProcessingException cause) {
            throw new IllegalArgumentException(format(ERROR_READING_ATTRIBUTE, attributeName, serialized), cause);
        }
    }

    private String readSerializedConfigurationFromEnvironment() {
        return configurationLoader.get();
    }
}
