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
import com.codeaffine.tiny.star.tck.ApplicationServerTestHelper;
import io.undertow.Undertow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import javax.net.ssl.SSLContext;
import java.io.InputStream;

import static com.codeaffine.tiny.star.spi.SecureSocketLayerConfiguration.SSL_PROTOCOL;
import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.*;
import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.HOST;
import static com.codeaffine.tiny.star.tck.ApplicationServerTestHelper.PORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class ProtocolListenerApplicatorTest {

    @Test
    void addListenerWithoutSslConfiguration() {
        ProtocolListenerApplicator protocolListenerApplicator = createAndConfigure(null);
        Undertow.Builder builder = mock(Undertow.Builder.class);

        protocolListenerApplicator.addListener(builder);

        verify(builder).addHttpListener(PORT, HOST);
    }

    @ParameterizedTest
    @CsvSource({
        "tiny.jks, store-password, tiny, key-password",
        "tiny-without-alias.jks, store-password, , key-password",
        "tiny-without-key-password.jks, store-password, tiny, ",
        "tiny.p12, store-password, tiny, ",
        "tiny-without-alias.p12, store-password, , ",
    })
    void addListenerWithSslConfiguration(String keyStoreLocation, String keyStorePassword, String keyAlias, String keyPassword) {
        ProtocolListenerApplicator protocolListenerApplicator = createAndConfigure(keyStoreLocation, keyStorePassword, keyAlias, keyPassword);
        Undertow.Builder builder = mock(Undertow.Builder.class);

        protocolListenerApplicator.addListener(builder);

        ArgumentCaptor<SSLContext> captor = forClass(SSLContext.class);
        verify(builder).addHttpsListener(eq(PORT), eq(HOST), captor.capture());
        assertThat(captor.getValue().getProtocol()).isEqualTo(SSL_PROTOCOL);
    }

    @Test
    void constructWithNullAsConfigurationArgument() {
        assertThatThrownBy(() -> new ProtocolListenerApplicator(null))
            .isInstanceOf(NullPointerException.class);
    }

    private ProtocolListenerApplicator createAndConfigure(
        String keyStoreLocation,
        String keyStorePassword,
        String keyAlias,
        String keyPassword)
    {
        InputStream keyStore = getClass().getClassLoader().getResourceAsStream(keyStoreLocation);
        return createAndConfigure(new SecureSocketLayerConfiguration(keyStore, keyStorePassword, keyAlias, keyPassword));
    }

    private static ProtocolListenerApplicator createAndConfigure(SecureSocketLayerConfiguration sslConfiguration) {
        return new ProtocolListenerApplicator(stubServerConfiguration(HOST, PORT, sslConfiguration));
    }

}
