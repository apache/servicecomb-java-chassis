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
package org.apache.servicecomb.common.rest.filter.inner;

import javax.servlet.http.Part;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class TestServerRestArgsFilter {

  final Invocation invocation = Mockito.mock(Invocation.class);

  final HttpServletResponseEx responseEx = Mockito.mock(HttpServletResponseEx.class);

  final Response response = Mockito.mock(Response.class);

  final Part part = Mockito.mock(Part.class);

  boolean invokedSendPart;

  final ServerRestArgsFilter filter = new ServerRestArgsFilter();

  @Test
  public void asyncBeforeSendResponse_part() {
    ResponsesMeta responsesMeta = new ResponsesMeta();
    responsesMeta.getResponseMap().put(202, RestObjectMapperFactory.getRestObjectMapper().constructType(Part.class));

    Mockito.when(responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE)).thenReturn(response);
    Mockito.when(response.getResult()).thenReturn(part);
    Mockito.when(response.getStatusCode()).thenReturn(202);
    Mockito.when(invocation.findResponseType(202)).thenReturn(TypeFactory.defaultInstance().constructType(Part.class));

    Mockito.doAnswer(invocationOnMock -> {
      invokedSendPart = true;
      return null;
    }).when(responseEx).sendPart(part);

    Assertions.assertNull(filter.beforeSendResponseAsync(invocation, responseEx));
    Assertions.assertTrue(invokedSendPart);
  }
}
