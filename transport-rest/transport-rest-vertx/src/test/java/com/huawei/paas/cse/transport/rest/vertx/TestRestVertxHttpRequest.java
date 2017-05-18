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

package com.huawei.paas.cse.transport.rest.vertx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class TestRestVertxHttpRequest {

    private RestVertxHttpRequest instance = null;

    @Before
    public void init() {
        instance = new RestVertxHttpRequest(getRoutingContext(), getFutureObject());
    }

    @Test
    public void testRestVertxHttpRequest() {
        Assert.assertNotNull(instance);

    }

    @Test
    public void testGetPath() {
        Deencapsulation.setField(instance, "request", getHttpServerRequest());
        Assert.assertNull(instance.getPath());
    }

    @Test
    public void testSetPathParamMap() {
        Map<String, String> pathParamMap = new HashMap<String, String>();
        pathParamMap.put("key", "value");
        instance.setPathParamMap(pathParamMap);
        Assert.assertNotNull(instance.getPathParam("key"));
    }

    @Test
    public void testGetQueryParam() {
        boolean status = true;
        try {

            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "request", httpServerRequest);
            MultiMap multiMap = Mockito.mock(MultiMap.class);
            Mockito.when(httpServerRequest.params()).thenReturn(multiMap);
            List<String> stringList = new ArrayList<String>();
            stringList.add("sters");
            Mockito.when(multiMap.getAll("key")).thenReturn(stringList);
            String[] str = instance.getQueryParam("key");
            Assert.assertEquals("sters", str[0]);
        } catch (Exception ex) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetQueryParamisNull() {
        boolean status = true;
        try {

            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "request", httpServerRequest);
            MultiMap multiMap = Mockito.mock(MultiMap.class);
            Mockito.when(httpServerRequest.params()).thenReturn(multiMap);
            List<String> stringList = null;

            Mockito.when(multiMap.getAll("key")).thenReturn(stringList);
            String[] str = instance.getQueryParam("key");
            Assert.assertNull(str);
        } catch (Exception ex) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetHeaderParam() {
        boolean status = false;
        try {
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "request", httpServerRequest);
            MultiMap multiMap = Mockito.mock(MultiMap.class);
            Mockito.when(httpServerRequest.headers()).thenReturn(multiMap);

            @SuppressWarnings({"unchecked"})
            Iterator<Entry<String, String>> iterator = Mockito.mock(Iterator.class);
            Mockito.when(multiMap.iterator()).thenReturn(iterator);
            Mockito.when(iterator.hasNext()).thenReturn(true).thenReturn(false);
            Assert.assertNotNull(instance.getHeaderParam("key"));
        } catch (Exception ex) {
            status = true;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetFormParam() {
        boolean status = false;
        try {
            Assert.assertNotNull(instance.getFormParam("key"));
        } catch (Exception ex) {
            status = true;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetCookieParam() {
        Cookie cookie = Mockito.mock(Cookie.class);
        RoutingContext context = Mockito.mock(RoutingContext.class);
        Deencapsulation.setField(instance, "context", context);
        Mockito.when(context.getCookie("key")).thenReturn(cookie);
        Assert.assertNull(instance.getCookieParam("key"));
    }

    @Test
    public void testGetBody() {
        boolean status = false;
        try {
            Assert.assertNull(instance.getBody());
        } catch (Exception ex) {
            status = true;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetQueryParams() {
        boolean status = true;
        try {
            HttpServerRequest httpServerRequest = Mockito.mock(HttpServerRequest.class);
            Deencapsulation.setField(instance, "request", httpServerRequest);
            MultiMap multiMap = Mockito.mock(MultiMap.class);
            Mockito.when(httpServerRequest.params()).thenReturn(multiMap);
            List<String> stringList = new ArrayList<String>();
            stringList.add("sters");
            Set<String> stringSet = new HashSet<String>();
            stringSet.add("sters");
            Mockito.when(multiMap.names()).thenReturn(stringSet);
            Mockito.when(multiMap.getAll("key")).thenReturn(stringList);
            Assert.assertNotNull(instance.getQueryParams());
        } catch (Exception ex) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetHttpRequest() {
        init();
        Assert.assertNull(instance.getHttpRequest());
    }

    private RoutingContext getRoutingContext() {
        return Mockito.mock(RoutingContext.class);

    }

    @SuppressWarnings("unchecked")
    private Future<Object> getFutureObject() {
        return Mockito.mock(Future.class);
    }

    private HttpServerRequest getHttpServerRequest() {
        return Mockito.mock(HttpServerRequest.class);
    }
}
