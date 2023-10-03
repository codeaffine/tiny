/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;

class ResourcesServletRegistrar {

    static final String SERVLET_NAME = "default";
    static final String SERVLET_CLASS_NAME = DefaultServlet.class.getName();
    static final int LOAD_ON_STARTUP = 0;
    static final String INIT_PARAMETER_DEBUG = "debug";
    static final String INIT_PARAMETER_LISTINGS = "listings";
    static final String INIT_VALUE_DEBUG = "0";
    static final String INIT_VALUE_LISTINGS = "false";
    static final String PATTERN = "/";

    void addResourcesServlet(Context context) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName(SERVLET_NAME);
        defaultServlet.setServletClass(SERVLET_CLASS_NAME);
        defaultServlet.addInitParameter(INIT_PARAMETER_DEBUG, INIT_VALUE_DEBUG);
        defaultServlet.addInitParameter(INIT_PARAMETER_LISTINGS, INIT_VALUE_LISTINGS);
        defaultServlet.setLoadOnStartup(LOAD_ON_STARTUP);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded(PATTERN, SERVLET_NAME);
    }

}
