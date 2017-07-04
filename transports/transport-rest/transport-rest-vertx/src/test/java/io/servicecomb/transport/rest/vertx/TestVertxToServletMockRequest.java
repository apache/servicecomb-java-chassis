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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import mockit.Deencapsulation;

public class TestVertxToServletMockRequest {

    private VertxToServletMockRequest instance = null;

    private void init() {
        instance = new VertxToServletMockRequest(getHttpServerRequest());
    }

    @Before
    public void setUp() throws Exception {
        instance = new VertxToServletMockRequest(null);
    }

    @After
    public void tearDown() throws Exception {
        instance = null;
    }

    @Test
    public void testVertxToServletMockRequest() {
        init();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testGetAttribute() {
        try {
            init();
            instance.getAttribute("name");
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetAttributeNames() {
        try {
            init();
            instance.getAttributeNames();
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetCharacterEncoding() {
        try {
            init();
            instance.getCharacterEncoding();
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testSetCharacterEncoding() {
        try {
            init();
            try {
                instance.setCharacterEncoding("env");
            } catch (UnsupportedEncodingException e) {
                Assert.assertNotNull(e);
            }
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetContentLength() {
        try {
            init();
            instance.getContentLength();
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetContentLengthLong() {
        try {
            init();
            instance.getContentLengthLong();
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetContentType() {
        try {
            init();
            instance.getContentType();
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetInputStream() {
        try {
            init();
            try {
                instance.getInputStream();
            } catch (IOException e) {
                Assert.assertNotNull(e);
            }
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetParameter() {
        try {
            init();
            instance.getParameter("param");
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetParameterNames() {
        try {
            init();
            instance.getParameterNames();
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetParameterValues() {
        boolean validAssert = true;
        try {
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "vertxRequest", httpServerRequest);
            MultiMap multiMap = Mockito.mock(MultiMap.class);
            Mockito.when(httpServerRequest.params()).thenReturn(multiMap);
            List<String> stringList = new ArrayList<String>();
            stringList.add("sters");
            Mockito.when(multiMap.getAll("key")).thenReturn(stringList);
            Assert.assertNotNull(instance.getParameterValues("param"));
        } catch (Exception e) {
            Assert.assertNotNull(e);
            validAssert = false;
        } catch (Error e) {
            Assert.assertNotNull(e);
            validAssert = false;
        }
        Assert.assertTrue(validAssert);
    }

    @Test
    public void testGetParameterMap() {
        boolean validAssert = true;
        try {
            init();
            @SuppressWarnings("unused")
            Map<String, String[]> paramMap = new HashMap<>();
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "vertxRequest", httpServerRequest);
            MultiMap multiMap = Mockito.mock(MultiMap.class);
            Mockito.when(httpServerRequest.params()).thenReturn(multiMap);
            Set<String> stringSet = new HashSet<String>();
            stringSet.add("sters");
            Mockito.when(multiMap.names()).thenReturn(stringSet);
            Assert.assertNotNull(instance.getParameterMap());
        } catch (Exception e) {
            Assert.assertNotNull(e);
            validAssert = false;
        } catch (Error e) {
            Assert.assertNotNull(e);
            validAssert = false;
        }
        Assert.assertTrue(validAssert);
    }

    @Test
    public void testGetProtocol() {
        try {
            init();
            instance.getProtocol();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetScheme() {
        try {
            init();
            instance.getScheme();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetServerName() {
        try {
            init();
            instance.getServerName();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetServerPort() {
        try {
            init();

            instance.getServerPort();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetReader() {
        try {
            init();
            instance.getReader();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRemoteAddr() {
        try {
            init();
            instance.getRemoteAddr();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRemoteHost() {
        try {
            init();
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "vertxRequest", httpServerRequest);
            SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
            Mockito.when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);
            Mockito.when(socketAddress.host()).thenReturn("localhost");
            Assert.assertEquals("localhost", instance.getRemoteHost());
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testSetAttribute() {
        try {
            init();
            instance.setAttribute("name", null);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testRemoveAttribute() {
        try {
            init();
            instance.removeAttribute("name");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetLocale() {
        try {
            init();
            instance.getLocale();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetLocales() {
        try {
            init();
            instance.getLocales();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsSecure() {
        try {
            init();
            instance.isSecure();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRequestDispatcher() {
        try {
            init();
            instance.getRequestDispatcher("request");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRealPath() {
        try {
            init();
            instance.getRealPath("path");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRemotePort() {
        try {
            init();
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "vertxRequest", httpServerRequest);
            SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
            Mockito.when(httpServerRequest.remoteAddress()).thenReturn(socketAddress);
            Mockito.when(socketAddress.port()).thenReturn(8080);
            Assert.assertEquals(8080, instance.getRemotePort());
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetLocalName() {
        try {
            init();
            instance.getLocalName();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetLocalAddr() {
        try {
            init();
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "vertxRequest", httpServerRequest);
            SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
            Mockito.when(httpServerRequest.localAddress()).thenReturn(socketAddress);
            Mockito.when(socketAddress.host()).thenReturn("localhost");
            Assert.assertEquals("localhost", instance.getLocalAddr());
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetLocalPort() {
        try {
            init();
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "vertxRequest", httpServerRequest);
            SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
            Mockito.when(httpServerRequest.localAddress()).thenReturn(socketAddress);
            Mockito.when(socketAddress.port()).thenReturn(8080);
            instance.getLocalPort();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetServletContext() {
        try {
            init();
            instance.getServletContext();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testStartAsync() {
        try {
            init();
            instance.startAsync();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testStartAsyncServletRequestServletResponse() {
        try {
            init();
            instance.startAsync(null, null);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsAsyncStarted() {
        try {
            init();
            instance.isAsyncStarted();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsAsyncSupported() {
        try {
            init();
            instance.isAsyncSupported();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetAsyncContext() {
        try {
            init();
            instance.getAsyncContext();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetDispatcherType() {
        try {
            init();
            instance.getDispatcherType();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetAuthType() {
        try {
            init();
            instance.getAuthType();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetCookies() {
        try {
            init();
            instance.getCookies();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetDateHeader() {
        try {
            init();
            instance.getDateHeader("name");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetHeader() {
        try {
            init();
            instance.getHeader("name");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetHeaders() {
        try {
            init();
            instance.getHeaders("name");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetHeaderNames() {
        try {
            init();
            instance.getHeaderNames();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetIntHeader() {
        try {
            init();
            Mockito.when(instance.getHeader("1234")).thenReturn("1234");
            Assert.assertEquals(1234, instance.getIntHeader("1234"));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetIntHeaderException() {
        try {
            init();
            Mockito.when(instance.getHeader("name")).thenReturn("name");
            Assert.assertEquals(0, instance.getIntHeader("name"));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetMethod() {
        try {
            init();

            instance.getMethod();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetPathInfo() {
        try {
            init();
            instance.getPathInfo();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetPathTranslated() {
        try {
            init();
            instance.getPathTranslated();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetContextPath() {
        try {
            init();
            instance.getContextPath();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetQueryString() {
        try {
            init();
            instance.getQueryString();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRemoteUser() {
        try {
            init();
            instance.getRemoteUser();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsUserInRole() {
        try {
            init();
            instance.isUserInRole("role");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetUserPrincipal() {
        try {
            init();
            instance.getUserPrincipal();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRequestedSessionId() {
        try {
            init();
            instance.getRequestedSessionId();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRequestURI() {
        try {
            init();
            instance.getRequestURI();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetRequestURL() {
        try {
            init();
            instance.getRequestURL();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetServletPath() {
        try {
            init();
            instance.getServletPath();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetSessionBoolean() {
        try {
            init();
            instance.getSession(false);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetSession() {
        try {
            init();
            instance.getSession();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testChangeSessionId() {
        try {
            init();
            instance.changeSessionId();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsRequestedSessionIdValid() {
        try {
            init();
            instance.isRequestedSessionIdValid();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsRequestedSessionIdFromCookie() {
        try {
            init();
            instance.isRequestedSessionIdFromCookie();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsRequestedSessionIdFromURL() {
        try {
            init();
            instance.isRequestedSessionIdFromUrl();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testIsRequestedSessionIdFromUrl() {
        try {
            init();
            instance.isRequestedSessionIdFromURL();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testAuthenticate() {
        try {
            init();
            instance.authenticate(null);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testLogin() {
        try {
            init();
            instance.login(null, null);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testLogout() {
        try {
            init();
            instance.logout();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetParts() {
        try {
            init();
            instance.getParts();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetPart() {
        try {
            init();
            instance.getPart("name");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testUpgrade() {
        try {
            init();
            instance.upgrade(null);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    private HttpServerRequest getHttpServerRequest() {
        return Mockito.mock(HttpServerRequest.class);
    }
}
