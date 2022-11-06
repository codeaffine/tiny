package com.codeaffine.tiny.star.servlet;

import static lombok.AccessLevel.PACKAGE;

import static java.util.Objects.isNull;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

import jakarta.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class JakartaToJavaxServletContextAdapter implements javax.servlet.ServletContext {

    @NonNull
    private final jakarta.servlet.ServletContext delegate;

    @Override
    public String getContextPath() {
        return delegate.getContextPath();
    }

    public javax.servlet.ServletContext getContext(String uripath) {
        return new JakartaToJavaxServletContextAdapter(delegate.getContext(uripath));
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion() {
        return delegate.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion() {
        return delegate.getEffectiveMinorVersion();
    }

    @Override
    public String getMimeType(String file) {
        return delegate.getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return delegate.getResourcePaths(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return delegate.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return delegate.getResourceAsStream(path);
    }

    public javax.servlet.RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    public javax.servlet.RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public javax.servlet.Servlet getServlet(String name) throws javax.servlet.ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public Enumeration<Servlet> getServlets() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public Enumeration<String> getServletNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String msg) {
        delegate.log(msg);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void log(Exception exception, String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String message, Throwable throwable) {
        delegate.log(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        return delegate.getRealPath(path);
    }

    @Override
    public String getServerInfo() {
        return delegate.getServerInfo();
    }

    @Override
    public String getInitParameter(String name) {
        return delegate.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return delegate.setInitParameter(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object object) {
        delegate.setAttribute(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        delegate.removeAttribute(name);
    }

    @Override
    public String getServletContextName() {
        return delegate.getServletContextName();
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws javax.servlet.ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.ServletRegistration getServletRegistration(String servletName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, javax.servlet.Filter filter) {
        throw new UnsupportedOperationException();
    }

    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends javax.servlet.Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    public <T extends javax.servlet.Filter> T createFilter(Class<T> clazz) throws javax.servlet.ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.FilterRegistration getFilterRegistration(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ? extends javax.servlet.FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSessionTrackingModes(Set<javax.servlet.SessionTrackingMode> sessionTrackingModes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<javax.servlet.SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<javax.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        delegate.addListener(className);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        delegate.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        delegate.addListener(listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws javax.servlet.ServletException {
        try {
            return delegate.createListener(clazz);
        } catch (ServletException e) {
            throw new javax.servlet.ServletException(e);
        }
    }

    @Override
    public javax.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        delegate.declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName() {
        return delegate.getVirtualServerName();
    }

    @Override
    public int getSessionTimeout() {
        return delegate.getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        delegate.setSessionTimeout(sessionTimeout);
    }

    @Override
    public String getRequestCharacterEncoding() {
        return delegate.getRequestCharacterEncoding();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        delegate.setRequestCharacterEncoding(encoding);
    }

    @Override
    public String getResponseCharacterEncoding() {
        return delegate.getResponseCharacterEncoding();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        delegate.setResponseCharacterEncoding(encoding);
    }
}
