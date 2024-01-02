/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.undertow;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import io.undertow.Undertow;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.SSL_PROTOCOL;
import static java.util.Objects.isNull;
import static javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.KeyManagerFactory.getInstance;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class ProtocolListenerApplicator {

    @NonNull
    private final ServerConfiguration configuration;

    Undertow.Builder addListener(Undertow.Builder builder) {
        if (isNull(configuration.getSecureSocketLayerConfiguration())) {
            builder.addHttpListener(configuration.getPort(), configuration.getHost());
        } else {
            builder.addHttpsListener(configuration.getPort(), configuration.getHost(), createSslContext());
        }
        return builder;
    }

    private SSLContext createSslContext() {
        try {
            SSLContext result = SSLContext.getInstance(SSL_PROTOCOL);
            result.init(getKeyManagers(), null, null);
            return result;
        } catch (NoSuchAlgorithmException | KeyManagementException cause) {
            throw extractExceptionToReport(cause, IllegalStateException::new);
        }
    }

    private KeyManager[] getKeyManagers() {
        SecureSocketLayerConfiguration sslConfiguration = configuration.getSecureSocketLayerConfiguration();
        try (InputStream inputStream = sslConfiguration.getKeyStore()) {
            KeyStore keyStore = KeyStore.getInstance(sslConfiguration.getKeyStoreType().name());
            keyStore.load(inputStream, sslConfiguration.getKeyStorePassword().toCharArray());
            KeyManagerFactory keyManagerFactory = getInstance(getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, sslConfiguration.getKeyPassword().toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception cause) {
            throw extractExceptionToReport(cause, IllegalStateException::new);
        }
    }
}
