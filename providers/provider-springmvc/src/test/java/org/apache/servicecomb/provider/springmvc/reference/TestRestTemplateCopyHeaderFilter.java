/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.provider.springmvc.reference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import mockit.Expectations;
import mockit.Mocked;

public class TestRestTemplateCopyHeaderFilter {
  RestTemplateCopyHeaderFilter filter = new RestTemplateCopyHeaderFilter();

  @Test
  public void getOrder() {
    Assert.assertEquals(Integer.MIN_VALUE, filter.getOrder());
  }

  @Test
  public void beforeSendRequestNoHeader(@Mocked Invocation invocation) {
    Map<String, Object> context = new HashMap<>();
    new Expectations() {
      {
        invocation.getHandlerContext();
        result = context;
      }
    };

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequest(invocation, requestEx);
    Assert.assertFalse(requestEx.getHeaderNames().hasMoreElements());
  }

  @Test
  public void beforeSendRequestWithNullHeader(@Mocked Invocation invocation) {
    Map<String, Object> context = new HashMap<>(1);
    HttpHeaders httpHeaders = new HttpHeaders();
    context.put(RestConst.CONSUMER_HEADER, httpHeaders);
    httpHeaders.add("headerName0", "headerValue0");
    httpHeaders.add("headerName1", null);
    httpHeaders.add("headerName2", "headerValue2");
    new Expectations() {
      {
        invocation.getHandlerContext();
        result = context;
      }
    };

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequest(invocation, requestEx);
    Assert.assertEquals("headerValue0", requestEx.getHeader("headerName0"));
    Assert.assertEquals("headerValue2", requestEx.getHeader("headerName2"));
    Assert.assertNull(requestEx.getHeader("headerName1"));
  }

  @Test
  public void beforeSendRequestHaveHeader(@Mocked Invocation invocation) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("name", "value");

    Map<String, Object> context = new HashMap<>();
    context.put(RestConst.CONSUMER_HEADER, httpHeaders);
    new Expectations() {
      {
        invocation.getHandlerContext();
        result = context;
      }
    };

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequest(invocation, requestEx);
    Assert.assertThat(Collections.list(requestEx.getHeaders("name")), Matchers.contains("value"));
  }

  @Test
  public void afterReceiveResponse() {
    Assert.assertNull(filter.afterReceiveResponse(null, null));
  }
}
