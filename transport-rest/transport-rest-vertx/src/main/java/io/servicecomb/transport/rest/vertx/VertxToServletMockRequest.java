/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.vertx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;

public class VertxToServletMockRequest implements HttpServletRequest {

    private HttpServerRequest vertxRequest;

    public VertxToServletMockRequest(HttpServerRequest vertxRequest) {
        this.vertxRequest = vertxRequest;
    }

    @Override
    public Object getAttribute(String name) {
        throw new Error("not supported method");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new Error("not supported method");
    }

    @Override
    public String getCharacterEncoding() {
        throw new Error("not supported method");
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new Error("not supported method");
    }

    @Override
    public int getContentLength() {
        throw new Error("not supported method");
    }

    @Override
    public long getContentLengthLong() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return this.vertxRequest.getHeader("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(String name) {
        return this.vertxRequest.getParam(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues(String name) {
        List<String> paramList = this.vertxRequest.params().getAll(name);
        return (String[]) paramList.toArray(new String[paramList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = new HashMap<>();
        MultiMap map = this.vertxRequest.params();
        for (String name : map.names()) {
            List<String> valueList = map.getAll(name);
            paramMap.put(name, (String[]) map.getAll(name).toArray(new String[valueList.size()]));
        }
        return paramMap;
    }

    @Override
    public String getProtocol() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return this.vertxRequest.scheme();
    }

    @Override
    public String getServerName() {
        throw new Error("not supported method");
    }

    @Override
    public int getServerPort() {
        throw new Error("not supported method");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new Error("not supported method");
    }

    @Override
    public String getRemoteAddr() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteHost() {
        return this.vertxRequest.remoteAddress().host();
    }

    @Override
    public void setAttribute(String name, Object o) {
        throw new Error("not supported method");
    }

    @Override
    public void removeAttribute(String name) {
        throw new Error("not supported method");
    }

    @Override
    public Locale getLocale() {
        throw new Error("not supported method");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isSecure() {
        throw new Error("not supported method");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealPath(String path) {
        return this.vertxRequest.path();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRemotePort() {
        return this.vertxRequest.remoteAddress().port();
    }

    @Override
    public String getLocalName() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalAddr() {
        return this.vertxRequest.localAddress().host();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLocalPort() {
        return this.vertxRequest.localAddress().port();
    }

    @Override
    public ServletContext getServletContext() {
        throw new Error("not supported method");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new Error("not supported method");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse) throws IllegalStateException {
        throw new Error("not supported method");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new Error("not supported method");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new Error("not supported method");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new Error("not supported method");
    }

    @Override
    public String getAuthType() {
        throw new Error("not supported method");
    }

    @Override
    public Cookie[] getCookies() {
        throw new Error("not supported method");
    }

    @Override
    public long getDateHeader(String name) {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeader(String name) {
        return this.vertxRequest.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        throw new Error("not supported method");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntHeader(String name) {
        String header = this.getHeader(name);
        int result;
        try {
            result = Integer.parseInt(header);
        } catch (NumberFormatException e) {
            return 0;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod() {
        return this.vertxRequest.method().name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathInfo() {
        return this.vertxRequest.path();
    }

    @Override
    public String getPathTranslated() {
        throw new Error("not supported method");
    }

    @Override
    public String getContextPath() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryString() {
        return this.vertxRequest.query();
    }

    @Override
    public String getRemoteUser() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new Error("not supported method");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new Error("not supported method");
    }

    @Override
    public String getRequestedSessionId() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestURI() {
        return this.vertxRequest.uri();
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new Error("not supported method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return this.getPathInfo();
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new Error("not supported method");
    }

    @Override
    public HttpSession getSession() {
        throw new Error("not supported method");
    }

    @Override
    public String changeSessionId() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new Error("not supported method");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new Error("not supported method");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new Error("not supported method");
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new Error("not supported method");
    }

    @Override
    public void logout() throws ServletException {
        throw new Error("not supported method");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new Error("not supported method");
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new Error("not supported method");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new Error("not supported method");
    }

}
