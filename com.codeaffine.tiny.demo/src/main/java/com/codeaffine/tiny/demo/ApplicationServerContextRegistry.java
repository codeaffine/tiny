/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.service.ApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

class ApplicationServerContextRegistry {

    private static final Object LOCK = new Object();

    private final ConcurrentHashMap<ApplicationConfiguration, ApplicationServerContext> configurationToContextMap;

    private ApplicationServerContextRegistry() {
        configurationToContextMap = new ConcurrentHashMap<>();
    }

    ApplicationServerContext get(ApplicationConfiguration configuration) {
        return configurationToContextMap.computeIfAbsent(configuration, ApplicationServerContext::new);
    }

    static ApplicationServerContextRegistry getApplicationServerContextRegistry() {
        synchronized (LOCK) {
            ApplicationContext applicationContext = RWT.getApplicationContext();
            ApplicationServerContextRegistry result = (ApplicationServerContextRegistry) applicationContext
                .getAttribute(ApplicationServerContextRegistry.class.getName());
            if (isNull(result)) {
                result = new ApplicationServerContextRegistry();
                applicationContext.setAttribute(ApplicationServerContextRegistry.class.getName(), result);
            }
            return result;
        }
    }

    static void stopAllServers(ApplicationServerContextRegistry registry) {
        registry.configurationToContextMap.values()
            .forEach(ApplicationServerContext::stopServer);
    }
}
