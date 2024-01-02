/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class FakeTrustManager extends X509ExtendedTrustManager {

    @NonNull
    private final Runnable serverTrustedCheckObserver;

    static SSLContext createSslContext(@NonNull Runnable serverTrustedCheckObserver) {
        try {
            SSLContext result = SSLContext.getInstance("TLS");
            result.init(null, new TrustManager[]{ new FakeTrustManager(serverTrustedCheckObserver) }, new SecureRandom());
            return result;
        } catch (NoSuchAlgorithmException | KeyManagementException cause) {
            throw new IllegalStateException(cause);
        }
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) { // NOSONAR: this is a fake trust manager
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) { // NOSONAR: this is a fake trust manager
        serverTrustedCheckObserver.run();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) { // NOSONAR: this is a fake trust manager
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) { // NOSONAR: this is a fake trust manager
        serverTrustedCheckObserver.run();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) { // NOSONAR: this is a fake trust manager
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) { // NOSONAR: this is a fake trust manager
        serverTrustedCheckObserver.run();
    }
}
