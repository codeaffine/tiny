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
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.codeaffine.tiny.star.Texts.ERROR_READING_SERVER_CONFIGURATION;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class MultiServerConfigurationReader implements ServerConfigurationReader {

    private static final Supplier<String> CONFIGURATION_READER = () -> System.getenv(ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @NonNull
    private final String applicationServerId;
    @NonNull
    private final Supplier<String> configurationLoader;
    @NonNull
    private final ApplicationConfiguration applicationConfiguration;

    private SingleServerConfigurationReader configurationReader;

    MultiServerConfigurationReader(String applicationServerId, ApplicationConfiguration applicationConfiguration) {
        this(applicationServerId, CONFIGURATION_READER, applicationConfiguration);
    }

    @Override
    public SecureSocketLayerConfiguration readSecureSocketLayerConfiguration() {
        ensureConfigurationReader();
        return configurationReader.readSecureSocketLayerConfiguration();
    }

    @Override
    public <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Class<T> type) {
        ensureConfigurationReader();
        return configurationReader.readEnvironmentConfigurationAttribute(attributeName, defaultValue, type);
    }

    @Override
    public <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Function<String, T> factory) {
        ensureConfigurationReader();
        return configurationReader.readEnvironmentConfigurationAttribute(attributeName, defaultValue, factory);
    }

    private void ensureConfigurationReader() {
        if (isNull(configurationReader)) {
            String serialized = configurationLoader.get();
            if (isNull(serialized)) {
                configurationReader = new SingleServerConfigurationReader(emptyMap(), applicationConfiguration);
            } else {
                configurationReader = initializeConfigurationReader(serialized);
            }
        }
    }

    private SingleServerConfigurationReader initializeConfigurationReader(String serialized) {
        Map<String, Object> attributeMap = readServerAttributeMap(serialized);
        if (isNull(attributeMap)) {
            attributeMap = emptyMap();
        }
        return new SingleServerConfigurationReader(attributeMap, applicationConfiguration);
    }

    private Map<String, Object> readServerAttributeMap(String serialized) {
        Map<String, Map<String, Object>> deserialized = deserialize(applicationServerId, serialized);
        return deserialized.get(applicationServerId);
    }

    @SuppressWarnings("unchecked")
    private static <V> Map<String, Map<String, V>> deserialize(String applicationServerId, String serialized) throws IllegalArgumentException {
        try {
            return OBJECT_MAPPER.readValue(serialized, Map.class);
        } catch (JsonProcessingException cause) {
            throw new IllegalArgumentException(format(ERROR_READING_SERVER_CONFIGURATION, applicationServerId, serialized), cause);
        }
    }
}
