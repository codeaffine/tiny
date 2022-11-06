package com.codeaffine.tiny.star;

import static lombok.AccessLevel.PRIVATE;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.function.Function;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
class ServerConfigurationReader {

    static final String ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION = "com.codeaffine.tiny.star.configuration";
    static final String ERROR_READING_ATTRIBUTE = "unable to read attribute %s from environment configuration %s";

    static <T> T readEnvironmentConfigurationAttribute(String attributeName, T defaultValue, Function<String, T> factory) {
        String serialized = System.getenv(ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION);
        if (nonNull(serialized)) {
            return readWithFactory(serialized, attributeName, defaultValue, factory);
        }
        return defaultValue;
    }

    static <T> T readEnvironmentConfigurationAttribute(String attributeName, T defaultValue, Class<T> type) {
        String serialized = System.getenv(ENVIRONMENT_APPLICATION_RUNNER_CONFIGURATION);
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
    private static <V> Map<String, V> deserialize(String attributeName, String serialized) {
        try {
            return new ObjectMapper().readValue(serialized, Map.class);
        } catch (JsonProcessingException cause) {
            throw new IllegalArgumentException(format(ERROR_READING_ATTRIBUTE, attributeName, serialized), cause);
        }
    }
}
