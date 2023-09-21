/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static com.codeaffine.tiny.star.ApplicationServer.CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION;
import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.CLASSPATH;
import static com.codeaffine.tiny.star.ApplicationServer.KeyStoreLocationType.FILESYSTEM;
import static com.codeaffine.tiny.star.Texts.ERROR_READING_ATTRIBUTE;
import static com.codeaffine.tiny.star.Texts.ERROR_WRONG_KEY_STORE_FILE_FORMAT;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class KeyStoreLoader {

    static final String KEY_STORE_LOCATION_SEGMENT_SEPARATOR = ":";

    static SecureSocketLayerConfiguration createSecureSocketLayerConfiguration(
        @NonNull String keyStoreLocation,
        @NonNull ApplicationConfiguration applicationConfiguration,
        @NonNull Function<InputStream, SecureSocketLayerConfiguration> factory)
    {
        String[] keyStorePathSegments = parseSegments(keyStoreLocation);
        if (CLASSPATH.name().equalsIgnoreCase(keyStorePathSegments[0])) {
            return createSecureSocketLayerConfigurationWithKeyStoreOnClasspath(keyStoreLocation, applicationConfiguration, factory, keyStorePathSegments[1]);
        } else if (FILESYSTEM.name().equalsIgnoreCase(keyStorePathSegments[0])) {
            return createSecureSocketLayerConfigurationWithKeyStoreOnFileSystem(keyStoreLocation, factory, keyStorePathSegments[1]);
        }
        String errorPattern = ERROR_READING_ATTRIBUTE + " " + "Unknown key store file location type %s.";
        throw new IllegalArgumentException(format(
            errorPattern,
            CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION,
            keyStoreLocation,
            keyStorePathSegments[0])
        );
    }

    private static String[] parseSegments(String keyStoreLocation) {
        String[] result = keyStoreLocation.split(KEY_STORE_LOCATION_SEGMENT_SEPARATOR);
        if (hasInvalidSegmentation(result)) {
            String errorPattern = ERROR_READING_ATTRIBUTE + " " + ERROR_WRONG_KEY_STORE_FILE_FORMAT;
            throw new IllegalArgumentException(format(errorPattern, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION, keyStoreLocation));
        }
        return result;
    }

    private static boolean hasInvalidSegmentation(String[] keyStorePathSegments) {
        return keyStorePathSegments.length != 2;
    }

    private static SecureSocketLayerConfiguration createSecureSocketLayerConfigurationWithKeyStoreOnClasspath(
        String keyStoreLocation,
        ApplicationConfiguration applicationConfiguration,
        Function<InputStream, SecureSocketLayerConfiguration> factory,
        String keyStorePath)
    {
        ClassLoader classLoader = applicationConfiguration.getClass().getClassLoader();
        try (InputStream keyStoreInputStream = classLoader.getResourceAsStream(keyStorePath)) {
            if (isNull(keyStoreInputStream)) {
                String message = ERROR_READING_ATTRIBUTE + " Key store location not found.";
                throw new IllegalArgumentException(format(message, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION, keyStoreLocation));
            }
            return factory.apply(keyStoreInputStream);
        } catch (IOException cause) {
            String message = format(ERROR_READING_ATTRIBUTE, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION, keyStoreLocation);
            throw new IllegalArgumentException(message, cause);
        }
    }

    private static SecureSocketLayerConfiguration createSecureSocketLayerConfigurationWithKeyStoreOnFileSystem(
        @NonNull String keyStoreLocation,
        @NonNull Function<InputStream,
        @NonNull SecureSocketLayerConfiguration> factory,
        @NonNull String keyStorePath)
    {
        try (FileInputStream fileInputStream = new FileInputStream(keyStorePath)) {
            return factory.apply(fileInputStream);
        } catch (IOException cause) {
            String message = format(ERROR_READING_ATTRIBUTE, CONFIGURATION_ATTRIBUTE_SECURE_SOCKET_LAYER_KEYSTORE_LOCATION, keyStoreLocation);
            throw new IllegalArgumentException(message, cause);
        }
    }
}
