package com.codeaffine.tiny.star.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;

class ResourcesServletRegistrar {

    static final String SERVLET_NAME = "default";
    static final String SERVLET_CLASS_NAME = DefaultServlet.class.getName();
    static final int LOAD_ON_STARTUP = 1;
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
