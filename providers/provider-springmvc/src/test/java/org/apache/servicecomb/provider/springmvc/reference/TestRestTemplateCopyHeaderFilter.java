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
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

public class TestRestTemplateCopyHeaderFilter {
  RestTemplateCopyHeaderFilter filter = new RestTemplateCopyHeaderFilter();

  Invocation invocation = Mockito.mock(Invocation.class);
  @Test
  public void getOrder() {
    Assertions.assertEquals(-10000, filter.getOrder());
  }

  @Test
  public void beforeSendRequestNoHeader() throws ExecutionException, InterruptedException {
    Map<String, Object> context = new HashMap<>();
    Mockito.when(invocation.getHandlerContext()).thenReturn(context);

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequestAsync(invocation, requestEx).get();
    Assertions.assertFalse(requestEx.getHeaderNames().hasMoreElements());
  }

  @Test
  public void beforeSendRequestWithNullHeader()
      throws ExecutionException, InterruptedException {
    Map<String, Object> context = new HashMap<>(1);
    HttpHeaders httpHeaders = new HttpHeaders();
    context.put(RestConst.CONSUMER_HEADER, httpHeaders);
    httpHeaders.add("headerName0", "headerValue0");
    httpHeaders.add("headerName1", null);
    httpHeaders.add("headerName2", "headerValue2");
    Mockito.when(invocation.getHandlerContext()).thenReturn(context);

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequestAsync(invocation, requestEx).get();
    Assertions.assertEquals("headerValue0", requestEx.getHeader("headerName0"));
    Assertions.assertEquals("headerValue2", requestEx.getHeader("headerName2"));
    Assertions.assertNull(requestEx.getHeader("headerName1"));
  }

  @Test
  public void beforeSendRequestHaveHeader()
      throws ExecutionException, InterruptedException {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("name", "value");

    Map<String, Object> context = new HashMap<>();
    context.put(RestConst.CONSUMER_HEADER, httpHeaders);
    Mockito.when(invocation.getHandlerContext()).thenReturn(context);

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequestAsync(invocation, requestEx).get();
    MatcherAssert.assertThat(Collections.list(requestEx.getHeaders("name")), Matchers.contains("value"));
  }

  @Test
  public void beforeSendRequestSkipContentLength()
      throws ExecutionException, InterruptedException {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.CONTENT_LENGTH, "0");

    Map<String, Object> context = new HashMap<>();
    context.put(RestConst.CONSUMER_HEADER, httpHeaders);
    Mockito.when(invocation.getHandlerContext()).thenReturn(context);

    HttpServletRequestEx requestEx = new CommonToHttpServletRequest(null, null, new HttpHeaders(), null, false);
    filter.beforeSendRequestAsync(invocation, requestEx).get();
    Assertions.assertNull((requestEx.getHeader(HttpHeaders.CONTENT_LENGTH)));
  }

  @Test
  public void afterReceiveResponse() {
    Assertions.assertNull(filter.afterReceiveResponse(null, null));
  }
}
