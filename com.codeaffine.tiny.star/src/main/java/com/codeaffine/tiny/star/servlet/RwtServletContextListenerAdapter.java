package com.codeaffine.tiny.star.servlet;

import org.eclipse.rap.rwt.engine.RWTServletContextListener;

import jakarta.servlet.ServletContextListener;

public class RwtServletContextListenerAdapter implements ServletContextListener {

    private final RWTServletContextListener delegate;

    public RwtServletContextListenerAdapter() {
        delegate = new RWTServletContextListener();
    }

    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent event) {
        delegate.contextInitialized(adaptToJavaxEvent(event));
    }

    @Override
    public void contextDestroyed(jakarta.servlet.ServletContextEvent event) {
        delegate.contextDestroyed(adaptToJavaxEvent(event));
    }

    private static javax.servlet.ServletContextEvent adaptToJavaxEvent(jakarta.servlet.ServletContextEvent event) {
        jakarta.servlet.ServletContext servletContext = event.getServletContext();
        return new javax.servlet.ServletContextEvent(new JakartaToJavaxServletContextAdapter(servletContext));
    }
}
