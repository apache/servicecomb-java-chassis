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

package org.apache.servicecomb.provider.rest.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestProducerHttpRequestArgMapper {
  Invocation invocation = Mockito.mock(Invocation.class);

  ProducerHttpRequestArgMapper mapper = new ProducerHttpRequestArgMapper("test", "test");

  @Test
  public void testGetFromContext() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Map<String, Object> context = new HashMap<>();
    context.put(RestConst.REST_REQUEST, request);

    Mockito.when(invocation.getHandlerContext()).thenReturn(context);

    Assertions.assertSame(request, mapper.createContextArg(invocation));
  }

  @Test
  public void testCreateFromInvocation() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    RestOperationMeta swaggerOperation = Mockito.mock(RestOperationMeta.class);
    Map<String, Object> context = new HashMap<>();

    Mockito.when(invocation.getHandlerContext()).thenReturn(context);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(swaggerOperation);

    Assertions.assertEquals(InvocationToHttpServletRequest.class, mapper.createContextArg(invocation).getClass());
  }
}
