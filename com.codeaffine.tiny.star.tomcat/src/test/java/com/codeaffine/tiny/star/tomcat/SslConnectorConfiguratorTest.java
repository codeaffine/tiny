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
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;

import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerTestHelper.HOST;
import static com.codeaffine.tiny.star.test.fixtures.ApplicationServerTestHelper.PORT;
import static org.apache.tomcat.util.net.SSLHostConfigCertificate.Type.UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class SslConnectorConfiguratorTest {

    private static final String DEFAULT_SSL_HOST_CONFIG_NAME = "defaultSSLHostConfigName";

    @ParameterizedTest
    @CsvSource({
        "tiny.jks, store-password, tiny, key-password",
        "tiny-without-alias.jks, store-password, , key-password",
        "tiny-without-key-password.jks, store-password, tiny, ",
        "tiny.p12, store-password, tiny, ",
        "tiny-without-alias.p12, store-password, , ",
    })
    void addListenerWithSslConfiguration(String keyStoreLocation, String keyStorePassword, String keyAlias, String keyPassword) throws IOException {
        ServerConfiguration serverConfiguration = stubServerConfiguration(keyStoreLocation, keyStorePassword, keyAlias, keyPassword);
        SslConnectorConfigurator configurator = new SslConnectorConfigurator(serverConfiguration);
        AbstractHttp11JsseProtocol<?> protocolHandler = stubProtocolHandler();
        Connector connector = stubConnector(protocolHandler);

        configurator.configureSsl(connector);


        SSLHostConfig sslHostConfig = captureSslHostConfig(protocolHandler);
        SSLHostConfigCertificate certificate = sslHostConfig.getCertificates().iterator().next();
        assertThat(sslHostConfig.getHostName()).isEqualTo(DEFAULT_SSL_HOST_CONFIG_NAME.toLowerCase());
        assertThat(sslHostConfig.getSslProtocol()).isEqualTo(SecureSocketLayerConfiguration.SSL_PROTOCOL);
        assertThat(sslHostConfig.getCertificates()).hasSize(1);
        assertThat(certificate.getType()).isEqualTo(UNDEFINED);
        assertThat(certificate.getCertificateKeystore()).isNotNull();
        assertThat(certificate.getCertificateKeystorePassword()).isNotEqualTo(serverConfiguration.getSecureSocketLayerConfiguration().getKeyStorePassword());
        assertThat(certificate.getCertificateKeyPassword()).isEqualTo(serverConfiguration.getSecureSocketLayerConfiguration().getKeyPassword());
        assertThat(certificate.getCertificateKeyAlias()).isEqualTo(serverConfiguration.getSecureSocketLayerConfiguration().getKeyAlias());
        assertThat(certificate.getCertificateKeyManager()).isNotNull();
        verify(protocolHandler).setSSLEnabled(true);
        verify(connector).setScheme(SslConnectorConfigurator.SCHEME);
        verify(connector).setSecure(true);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new SslConnectorConfigurator(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void configureSslWithNullAsArgumentNameArgument() {
        SslConnectorConfigurator configurator = new SslConnectorConfigurator(mock(ServerConfiguration.class));

        assertThatThrownBy(() -> configurator.configureSsl(null))
            .isInstanceOf(NullPointerException.class);
    }
    private ServerConfiguration stubServerConfiguration(String keyStoreLocation, String keyStorePassword, String keyAlias, String keyPassword) {
        InputStream keyStore = getClass().getClassLoader().getResourceAsStream(keyStoreLocation);
        return stubServerConfiguration(new SecureSocketLayerConfiguration(keyStore, keyStorePassword, keyAlias, keyPassword));
    }

    private static ServerConfiguration stubServerConfiguration(SecureSocketLayerConfiguration sslConfiguration) {
        ServerConfiguration result = mock(ServerConfiguration.class);
        when(result.getPort()).thenReturn(PORT);
        when(result.getHost()).thenReturn(HOST);
        when(result.getSecureSocketLayerConfiguration()).thenReturn(sslConfiguration);
        return result;
    }

    private static AbstractHttp11JsseProtocol<?> stubProtocolHandler() {
        AbstractHttp11JsseProtocol<?> result = mock(AbstractHttp11JsseProtocol.class);
        when(result.getDefaultSSLHostConfigName()).thenReturn(DEFAULT_SSL_HOST_CONFIG_NAME);
        return result;
    }

    private static Connector stubConnector(AbstractHttp11JsseProtocol<?> protocolHandler) {
        Connector result = mock(Connector.class);
        when(result.getProtocolHandler()).thenReturn(protocolHandler);
        return result;
    }

    private static SSLHostConfig captureSslHostConfig(AbstractHttp11JsseProtocol<?> protocolHandler) {
        ArgumentCaptor<SSLHostConfig> sslHostConfigCaptor = forClass(SSLHostConfig.class);
        verify(protocolHandler).addSslHostConfig(sslHostConfigCaptor.capture());
        return sslHostConfigCaptor.getValue();
    }
}
