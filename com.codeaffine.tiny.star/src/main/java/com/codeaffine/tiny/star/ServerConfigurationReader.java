/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.codeaffine.tiny.star.Texts.ERROR_READING_ATTRIBUTE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class ServerConfigurationReader {

    static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = "com.codeaffine.tiny.star.configuration";

    private static final Supplier<String> CONFIGURATION_READER = () -> System.getenv(ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION);
    private static final AtomicReference<Supplier<String>> CONFIGURATION_READER_HOLDER = new AtomicReference<>(CONFIGURATION_READER);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static void setConfigurationReader(@NonNull Supplier<String> configurationReader) {
        CONFIGURATION_READER_HOLDER.set(configurationReader);
    }

    static void resetConfigurationReader() {
        CONFIGURATION_READER_HOLDER.set(CONFIGURATION_READER);
    }

    static <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Function<String, T> factory) {
        String serialized = readSerializedConfigurationFromEnvironment();
        if (nonNull(serialized)) {
            return readWithFactory(serialized, attributeName, defaultValue, factory);
        }
        return defaultValue;
    }

    static <T> T readEnvironmentConfigurationAttribute(@NonNull String attributeName, T defaultValue, @NonNull Class<T> type) {
        String serialized = readSerializedConfigurationFromEnvironment();
        if (nonNull(serialized)) {
            return readWithTypeCast(attributeName, defaultValue, type, serialized);
        }
        return defaultValue;
    }

    private static <T> T readWithFactory(String serialized, String attributeName, T defaultValue, Function<String, T> factory) {
        Map<String, String> map = deserialize(attributeName, serialized);
        String value = map.get(attributeName);
        if (nonNull(value)) {
            return factory.apply(value);
        }
        return defaultValue;
    }

    private static <T> T readWithTypeCast(String attributeName, T defaultValue, Class<T> type, String serialized) {
        Map<String, Object> map = deserialize(attributeName, serialized);
        return type.cast(map.getOrDefault(attributeName, defaultValue));
    }

    @SuppressWarnings("unchecked")
    private static <V> Map<String, V> deserialize(String attributeName, String serialized) throws IllegalArgumentException {
        try {
            return OBJECT_MAPPER.readValue(serialized, Map.class);
        } catch (JsonProcessingException cause) {
            throw new IllegalArgumentException(format(ERROR_READING_ATTRIBUTE, attributeName, serialized), cause);
        }
    }

    private static String readSerializedConfigurationFromEnvironment() {
        return CONFIGURATION_READER_HOLDER.get()
            .get();
    }
}
