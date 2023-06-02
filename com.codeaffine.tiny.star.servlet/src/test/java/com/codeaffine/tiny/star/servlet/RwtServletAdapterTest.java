/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class RwtServletAdapterTest {

    private static final String SERVLET_NAME = "servlet-name";
    private RWTServlet delegate;
    private RwtServletAdapter adapter;

    @BeforeEach
    void setUp() {
        delegate = mock(RWTServlet.class);
        adapter = new RwtServletAdapter(delegate);
    }

    @Test
    void init() throws Exception {
        ServletConfig jakartaServletConfig = mock(ServletConfig.class);

        adapter.init(jakartaServletConfig);

        ArgumentCaptor<javax.servlet.ServletConfig> captor = forClass(javax.servlet.ServletConfig.class);
        verify(delegate).init(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(JakartaToJavaxServletConfigAdapter.class);
    }

    @Test
    void initWithProblem() throws Exception {
        ServletException problem = new ServletException();
        stubRwtDelegateInitWithProblem(problem);

        Throwable actual = catchThrowable(() -> adapter.init(mock(ServletConfig.class)));

        assertThat(actual)
            .isInstanceOf(jakarta.servlet.ServletException.class)
            .hasCause(problem);
    }

    @Test
    void getServletConfig() {
        ServletConfig servletConfig = stubServletConfig();
        stubRwtDelegateWithServletConfig(servletConfig);

        ServletConfig actual = adapter.getServletConfig();
        String actualServletName = actual.getServletName();

        assertThat(actualServletName).isEqualTo(SERVLET_NAME);
    }

    @Test
    void service() throws Exception {
        adapter.service(mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        verify(delegate).service(any(), any());
    }
    
    @Test
    void serviceWithProblem() throws Exception {
        ServletException problem = new ServletException();
        stubRwtDelegateServiceWithProblem(problem);

        Throwable actual = catchThrowable(() -> adapter.service(mock(HttpServletRequest.class), mock(HttpServletResponse.class)));

        assertThat(actual)
            .isInstanceOf(jakarta.servlet.ServletException.class)
            .hasCause(problem);
    }
    
    @Test
    void serviceWithNullAsRequestArgument() {
        assertThatThrownBy(() -> adapter.service(null, mock(HttpServletResponse.class)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void serviceWithNullAsResponseArgument() {
        assertThatThrownBy(() -> adapter.service(mock(HttpServletRequest.class), null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new RwtServletAdapter(null))
            .isInstanceOf(NullPointerException.class);
    }

    private void stubRwtDelegateServiceWithProblem(ServletException problem) throws ServletException, IOException {
        doThrow(problem).when(delegate).service(any(), any());
    }

    private void stubRwtDelegateInitWithProblem(ServletException problem) throws ServletException {
        doThrow(problem).when(delegate).init(any());
    }

    private void stubRwtDelegateWithServletConfig(ServletConfig servletConfig) {
        JakartaToJavaxServletConfigAdapter jakartaToJavaxServletConfigAdapter = new JakartaToJavaxServletConfigAdapter(servletConfig);
        when(delegate.getServletConfig()).thenReturn(jakartaToJavaxServletConfigAdapter);
    }

    private static ServletConfig stubServletConfig() {
        ServletConfig result = mock(ServletConfig.class);
        when(result.getServletName()).thenReturn(SERVLET_NAME);
        return result;
    }
}
