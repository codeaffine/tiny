/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
module tiny.com.codeaffine.tiny.star.logback {

    requires static lombok;

    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.codeaffine.tiny.shared;
    requires com.codeaffine.tiny.star;
    requires org.slf4j;

    provides com.codeaffine.tiny.star.spi.LoggingFrameworkControlFactory
        with com.codeaffine.tiny.star.logback.LogbackConfigurator;
    provides ch.qos.logback.classic.spi.Configurator
        with com.codeaffine.tiny.star.logback.LogbackConfigurator;
}
