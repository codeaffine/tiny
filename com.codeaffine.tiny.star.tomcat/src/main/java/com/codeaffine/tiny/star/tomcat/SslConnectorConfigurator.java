/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration;
import com.codeaffine.tiny.star.spi.ServerConfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.SSL_PROTOCOL;
import static java.util.Objects.nonNull;
import static javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.KeyManagerFactory.getInstance;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.tomcat.util.net.SSLHostConfigCertificate.Type.UNDEFINED;

@RequiredArgsConstructor(access = PACKAGE)
class SslConnectorConfigurator {

    static final String SCHEME = "https";

    @NonNull
    private final ServerConfiguration configuration;

    void configureSsl(@NonNull Connector connector) {
        SecureSocketLayerConfiguration sslConfiguration = configuration.getSecureSocketLayerConfiguration();
        SSLHostConfig sslHostConfig = createAndConfigureSslHostConfig(connector);
        createAndConfigureSslCertificate(sslHostConfig, sslConfiguration);
        connector.setScheme(SCHEME);
        connector.setSecure(true);
    }

    private static SSLHostConfig createAndConfigureSslHostConfig(Connector connector) {
        AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
        protocol.setSSLEnabled(true);
        SSLHostConfig result = new SSLHostConfig();
        result.setHostName(protocol.getDefaultSSLHostConfigName());
        result.setSslProtocol(SSL_PROTOCOL);
        protocol.addSslHostConfig(result);
        return result;
    }

    private static void createAndConfigureSslCertificate(SSLHostConfig sslHostConfig, SecureSocketLayerConfiguration sslConfiguration) {
        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, UNDEFINED);
        try (InputStream inputStream = sslConfiguration.getKeyStore()) {
            initializeCertificate(certificate, sslConfiguration, inputStream);
        } catch (IOException cause) {
            throw extractExceptionToReport(cause, IllegalStateException::new);
        }
        sslHostConfig.addCertificate(certificate);
    }

    private static void initializeCertificate(SSLHostConfigCertificate certificate, SecureSocketLayerConfiguration sslConfiguration, InputStream inputStream) {
        KeyStore keystore = loadKeyStore(sslConfiguration, inputStream);
        certificate.setCertificateKeystore(keystore);
        if (nonNull(sslConfiguration.getKeyAlias())) {
            certificate.setCertificateKeyAlias(sslConfiguration.getKeyAlias());
        }
        certificate.setCertificateKeyPassword(sslConfiguration.getKeyPassword());
        certificate.setCertificateKeyManager(loadKeyManager(sslConfiguration, keystore));
    }

    private static KeyStore loadKeyStore(SecureSocketLayerConfiguration sslConfiguration, InputStream inputStream) {
        try {
            KeyStore result = KeyStore.getInstance(sslConfiguration.getKeyStoreType().name());
            result.load(inputStream, sslConfiguration.getKeyStorePassword().toCharArray());
            return result;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException cause) {
            throw extractExceptionToReport(cause, reason -> new IllegalStateException("Failed to load key store", reason));
        }
    }

    private static X509KeyManager loadKeyManager(SecureSocketLayerConfiguration sslConfiguration, KeyStore keyStore) {
        try {
            KeyManagerFactory keyManagerFactory = getInstance(getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, sslConfiguration.getKeyPassword().toCharArray());
            return (X509KeyManager) keyManagerFactory.getKeyManagers()[0];
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException cause) {
            throw extractExceptionToReport(cause, reason -> new IllegalStateException("Failed to load key manager", reason));
        }
    }
}
