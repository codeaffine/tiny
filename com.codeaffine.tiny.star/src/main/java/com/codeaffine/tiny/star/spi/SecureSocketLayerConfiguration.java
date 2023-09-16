/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.spi;

import lombok.Getter;
import lombok.NonNull;

import javax.net.ssl.KeyManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.KeyStoreType.JKS;
import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.KeyStoreType.PKCS12;
import static com.codeaffine.tiny.star.spi.Texts.*;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.KeyManagerFactory.getInstance;

/**
 * <p>Configuration for a secure socket layer. It is assumed that the key store contains a single key entry with the given key alias.
 * The key store type and password are used to load the key store. The key password is used to access the key entry.</p>
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">Customizing Stores</a>,
 *      <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">KeyStore</a>.
 */
@Getter
public class SecureSocketLayerConfiguration {

    /**
     * The protocol that is used to establish a secure connection.
     */
    public static final String SSL_PROTOCOL = "TLS";

    static final int JKS_MAGIC = 0xfeedfeed;
    static final int SEQUENCE = 0x30000000;

    /**
     * The supported key store types.
     */
    public enum KeyStoreType {
        JKS,
        PKCS12
    }

    private final InputStream keyStore;
    private final KeyStoreType keyStoreType;
    private final String keyStorePassword;
    private final String keyAlias;
    private final String keyPassword;

    /**
     * Creates a new instance with the given key store, key store password, key alias and key password. The key store type is detected automatically.
     * Supported key store types are {@link KeyStoreType#JKS} and {@link KeyStoreType#PKCS12}.
     *
     * @param keyStore an input stream that contains the key store. Must not be {@code null}. The stream is closed after the key store has been loaded.
     * @param keyStorePassword the password of the key store. Must not be {@code null}.
     * @param keyAlias the alias of the key entry.
     * @param keyPassword the password of the key entry. If {@code null}, the key store password is used.
     */
    public SecureSocketLayerConfiguration(
        @NonNull InputStream keyStore,
        @NonNull String keyStorePassword,
        String keyAlias,
        String keyPassword)
    {
        byte[] bytes = readAllBytes(keyStore);
        this.keyStore = new ByteArrayInputStream(bytes);
        this.keyStoreType = extractKeyStoreType(bytes);
        this.keyStorePassword = keyStorePassword;
        this.keyAlias = keyAlias;
        this.keyPassword = isNull(keyPassword) ? keyStorePassword : keyPassword;
        KeyStore loadedKeyStore = verifyKeyStorePassword(bytes, keyStorePassword, keyStoreType);
        verifyKeyPassword(loadedKeyStore, this.keyPassword);
        verifyAlias(loadedKeyStore, this.keyAlias);
    }

    private static byte[] readAllBytes(InputStream keyStore) {
        try (InputStream inputStream = keyStore) {
            return keyStore.readAllBytes();
        } catch (IOException ioe) {
            throw extractExceptionToReport(ioe, cause -> new IllegalArgumentException(ERROR_READING_KEY_STORE, cause));
        }
    }

    private static KeyStoreType extractKeyStoreType(byte[] keyStoreBytes) {
        if(isNull(keyStoreBytes) || keyStoreBytes.length < 4) {
            throw new IllegalArgumentException(ERROR_INVALID_KEY_STORE);
        }
        int firstInt = (
              (0xff & keyStoreBytes[0]) << 24
            | (0xff & keyStoreBytes[1]) << 16
            | (0xff & keyStoreBytes[2]) << 8
            | (0xff & keyStoreBytes[3])
        );
        if (JKS_MAGIC == firstInt) {
            return JKS;
        } else if (SEQUENCE == (firstInt & 0xff000000)) {
            return PKCS12;
        } else {
            throw new IllegalArgumentException(ERROR_UNABLE_TO_DETECT_KEYSTORE_TYPE);
        }
    }

    private static KeyStore verifyKeyStorePassword(byte[] bytes, String keyStorePassword, KeyStoreType keyStoreType) {
        try {
            KeyStore result = KeyStore.getInstance(keyStoreType.name());
            result.load(new ByteArrayInputStream(bytes), keyStorePassword.toCharArray());
            return result;
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException cause) {
            throw extractExceptionToReport(cause, rootCause -> new IllegalArgumentException(ERROR_INVALID_KEY_STORE_PASSWORD, rootCause));
        }
    }

    private static void verifyKeyPassword(KeyStore loadedKeyStore, String keyPassword)  {
        try {
            KeyManagerFactory keyManagerFactory = getInstance(getDefaultAlgorithm());
            keyManagerFactory.init(loadedKeyStore, keyPassword.toCharArray());
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException cause) {
            throw extractExceptionToReport(cause, rootCause -> new IllegalArgumentException(ERROR_INVALID_KEY_PASSWORD, rootCause));
        }
    }

    private static void verifyAlias(KeyStore loadedKeyStore, String keyAlias) {
        try {
            if (nonNull(keyAlias) && (!loadedKeyStore.isKeyEntry(keyAlias))) {
                throw new IllegalArgumentException(format(ERROR_ALIAS_NOT_FOUND, keyAlias));
            }
        } catch (KeyStoreException e) {
            throw extractExceptionToReport(e, rootCause -> new IllegalArgumentException(format(ERROR_VERIFYING_ALIAS, keyAlias), rootCause));
        }
    }
}
