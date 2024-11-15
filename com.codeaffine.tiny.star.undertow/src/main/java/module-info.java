/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
module com.codeaffine.tiny.star.undertow {

    requires static lombok;

    requires com.codeaffine.tiny.shared;
    requires com.codeaffine.tiny.star;
    requires jakarta.servlet;
    requires jdk.unsupported;
    requires org.eclipse.rap.rwt;
    requires undertow.core;
    requires undertow.servlet;

    provides com.codeaffine.tiny.star.spi.ServerFactory
        with com.codeaffine.tiny.star.undertow.ServerFactoryImpl;
}
