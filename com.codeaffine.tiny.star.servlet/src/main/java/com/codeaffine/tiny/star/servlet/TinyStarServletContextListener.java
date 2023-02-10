package com.codeaffine.tiny.star.servlet;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ApplicationRunner;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TinyStarServletContextListener implements ServletContextListener {

    private final ApplicationConfiguration configuration;

    private ApplicationRunner applicationRunner;

    @Override
    public void contextInitialized( ServletContextEvent event ) {
        ServletContext servletContext = event.getServletContext();
        applicationRunner = new ApplicationRunner(configuration, new JakartaToJavaxServletContextAdapter(servletContext));
        applicationRunner.start();
    }

    @Override
    public void contextDestroyed( ServletContextEvent event ) {
        applicationRunner.stop();
        applicationRunner = null;
    }
}
