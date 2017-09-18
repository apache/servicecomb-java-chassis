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

package io.servicecomb.provider.rest.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import mockit.Expectations;
import mockit.Mocked;

public class TestProducerHttpRequestArgMapper {
  @Mocked
  Invocation invocation;

  ProducerHttpRequestArgMapper mapper = new ProducerHttpRequestArgMapper(0);

  @Test
  public void testGetFromContext(@Mocked HttpServletRequest request) {
    Map<String, Object> context = new HashMap<>();
    context.put(RestConst.REST_REQUEST, request);

    new Expectations() {
      {
        invocation.getHandlerContext();
        result = context;
      }
    };

    Assert.assertSame(request, mapper.createContextArg(invocation));
  }

  @Test
  public void testCreateFromInvocation(@Mocked HttpServletRequest request, @Mocked OperationMeta operationMeta,
      @Mocked RestOperationMeta swaggerOperation) {
    Map<String, Object> context = new HashMap<>();

    new Expectations() {
      {
        invocation.getHandlerContext();
        result = context;
        invocation.getOperationMeta();
        result = operationMeta;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = swaggerOperation;
      }
    };

    Assert.assertEquals(InvocationToHttpServletRequest.class, mapper.createContextArg(invocation).getClass());
  }
}
