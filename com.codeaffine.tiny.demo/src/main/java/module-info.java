/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
open module com.codeaffine.tiny.demo {

    requires com.codeaffine.tiny.star.cli;
    requires com.codeaffine.tiny.star;
    requires org.eclipse.rap.rwt;
    requires org.slf4j;

    requires /*runtime*/ com.codeaffine.tiny.star.cli.basic;
    requires /*runtime*/ com.codeaffine.tiny.star.logback;
//    requires /*runtime*/ com.codeaffine.tiny.star.log4j;
    requires /*runtime*/ com.codeaffine.tiny.star.undertow;
    requires static lombok;
//    requires /*runtime*/ com.codeaffine.tiny.star.tomcat;

    exports com.codeaffine.tiny.demo;
}
