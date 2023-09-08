/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
module com.codeaffine.tiny.star.tomcat {
    requires static lombok;

    requires jakarta.servlet;
    requires com.codeaffine.tiny.star;
    requires com.codeaffine.tiny.shared;
    requires com.codeaffine.tiny.star.servlet;
    requires org.eclipse.rap.rwt;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.jasper;

    provides com.codeaffine.tiny.star.spi.ServerFactory
        with com.codeaffine.tiny.star.tomcat.ServerFactoryImpl;
}
