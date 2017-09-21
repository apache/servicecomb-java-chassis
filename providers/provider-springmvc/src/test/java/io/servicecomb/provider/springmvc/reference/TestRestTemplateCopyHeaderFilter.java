/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.reference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
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
