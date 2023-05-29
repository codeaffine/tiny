/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.servlet;

import org.eclipse.rap.rwt.engine.RWTServlet;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.Serial;

public class RwtServletAdapter extends jakarta.servlet.http.HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    private final RWTServlet delegate;

    public RwtServletAdapter() {
        delegate = new RWTServlet();
    }

    @Override
    public void init(jakarta.servlet.ServletConfig config) throws jakarta.servlet.ServletException {
        try {
            delegate.init(new JakartaToJavaxServletConfigAdapter(config));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public jakarta.servlet.ServletConfig getServletConfig() {
        JakartaToJavaxServletConfigAdapter servletConfiguration = (JakartaToJavaxServletConfigAdapter) delegate.getServletConfig();
        return servletConfiguration.delegate;
    }

    @Override
    protected void service(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp)
        throws ServletException, IOException
    {
        try {
            delegate.service(new JakartaToJavaxServletRequestAdapter(req), new JakartaToJavaxServletResponseAdapter(resp));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e);
        }
    }
}
