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

package io.servicecomb.provider.rest.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.definition.RestParam;
import io.servicecomb.common.rest.definition.path.URLPathBuilder;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;

public class TestGenericServletMockRequest {

    @Test
    public void testGenericServletMockRequest()
        throws Exception {

        Invocation invocation = Mockito.mock(Invocation.class);
        OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
        RestOperationMeta swaggerOperation = Mockito.mock(RestOperationMeta.class);
        Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerOperation);
        GenericServletMockRequest lGenericServletMockRequest = new GenericServletMockRequest(invocation);
        URLPathBuilder uRLPathBuilder = Mockito.mock(URLPathBuilder.class);
        Mockito.when(swaggerOperation.getPathBuilder()).thenReturn(uRLPathBuilder);
        Mockito.when(swaggerOperation.getPathBuilder().createPathString(null)).thenReturn("test");

        RestParam param = Mockito.mock(RestParam.class);
        Mockito.when(swaggerOperation.getParamByName("1")).thenReturn(param);
        Mockito.when(param.getValue(null)).thenReturn("1");
        List<RestParam> restParam = new ArrayList<>();
        restParam.add(param);
        Mockito.when(swaggerOperation.getParamList()).thenReturn(restParam);
        Assert.assertEquals("1", lGenericServletMockRequest.getParameter("1"));
        Assert.assertNull(lGenericServletMockRequest.getParameterValues("1"));
        Assert.assertEquals("1", lGenericServletMockRequest.getHeader("1"));
        Assert.assertEquals(1, lGenericServletMockRequest.getIntHeader("1"));
        Assert.assertEquals("test", lGenericServletMockRequest.getServletPath());
        Mockito.when(swaggerOperation.getParamByName("test")).thenReturn(null);
        Assert.assertEquals(null, lGenericServletMockRequest.getParameter("test"));
        Assert.assertNull(lGenericServletMockRequest.getParameterValues("test"));
        Map<String, String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put(null, null);
        Assert.assertEquals(paramMap, lGenericServletMockRequest.getParameterMap());
        try {
            lGenericServletMockRequest.getAttribute("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getAttributeNames();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getCharacterEncoding();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.setCharacterEncoding("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getContentLength();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getContentLengthLong();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getContentType();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getInputStream();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getParameterNames();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getProtocol();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getScheme();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getServerName();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getServerPort();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getReader();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRemoteAddr();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRemoteHost();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.setAttribute("", "");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.removeAttribute("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getLocale();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getLocales();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isSecure();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRequestDispatcher("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRealPath("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRemotePort();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getLocalName();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getLocalAddr();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getLocalPort();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getServletContext();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.startAsync();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.startAsync(null, null);
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isAsyncStarted();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isAsyncSupported();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getAsyncContext();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getDispatcherType();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getAuthType();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getCookies();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getDateHeader("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getHeaders("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getHeaderNames();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getPathTranslated();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getContextPath();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getQueryString();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRemoteUser();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isUserInRole("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getUserPrincipal();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRequestedSessionId();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRequestURI();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getRequestURL();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getSession(true);
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getSession();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.changeSessionId();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isRequestedSessionIdValid();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isRequestedSessionIdFromCookie();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isRequestedSessionIdFromURL();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.isRequestedSessionIdFromUrl();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.authenticate(null);
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.login("", "");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.logout();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getParts();
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.getPart("");
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }
        try {
            lGenericServletMockRequest.upgrade(null);
        } catch (Error e) {
            Assert.assertEquals("not supported method", e.getMessage());
        }

        Assert.assertNull(lGenericServletMockRequest.getMethod());
        Assert.assertEquals("test", lGenericServletMockRequest.getPathInfo());
    }

}
