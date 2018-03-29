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

package org.apache.servicecomb.transport.rest.servlet;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestProducerInvocation;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;
import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestServletProducerInvocation {
  @Mocked
  OperationMeta operationMeta;

  @Mocked
  RestOperationMeta restOperationMeta;

  RestServletProducerInvocation restInvocation = new RestServletProducerInvocation();

  @Test
  public void findRestOperationCacheTrue(@Mocked HttpServletRequest request, @Mocked HttpServerFilter f1) {
    HttpServletRequestEx requestEx = new StandardHttpServletRequestEx(request);
    Deencapsulation.setField(restInvocation, "requestEx", requestEx);

    new MockUp<RestProducerInvocation>() {
      @Mock
      void findRestOperation() {
        Deencapsulation.setField(getMockInstance(), "restOperationMeta", restOperationMeta);
      }
    };

    List<HttpServerFilter> httpServerFilters = Arrays.asList(f1);
    new Expectations() {
      {
        f1.needCacheRequest(operationMeta);
        result = true;
      }
    };

    restInvocation.setHttpServerFilters(httpServerFilters);

    restInvocation.findRestOperation();
    Assert.assertTrue(Deencapsulation.getField(requestEx, "cacheRequest"));
  }

  @Test
  public void collectCacheRequestCacheTrue(@Mocked HttpServerFilter f1) {
    List<HttpServerFilter> httpServerFilters = Arrays.asList(f1);
    new Expectations() {
      {
        f1.needCacheRequest(operationMeta);
        result = true;
      }
    };

    restInvocation.setHttpServerFilters(httpServerFilters);
    Assert.assertTrue(restInvocation.collectCacheRequest(operationMeta));
  }

  @Test
  public void collectCacheRequestCacheFalse(@Mocked HttpServerFilter f1) {
    List<HttpServerFilter> httpServerFilters = Arrays.asList(f1);
    new Expectations() {
      {
        f1.needCacheRequest(operationMeta);
        result = false;
      }
    };

    restInvocation.setHttpServerFilters(httpServerFilters);
    Assert.assertFalse(restInvocation.collectCacheRequest(operationMeta));
  }

}
