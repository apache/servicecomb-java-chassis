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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.provider.springmvc.reference.UrlWithProviderPrefixClientHttpRequestFactory.UrlWithProviderPrefixClientHttpRequest;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;

public class TestUrlWithProviderPrefixClientHttpRequestFactory {
  UrlWithProviderPrefixClientHttpRequestFactory factory = new UrlWithProviderPrefixClientHttpRequestFactory("/a/b/c");

  URI uri = URI.create("cse://ms/a/b/c/v1/path");

  @Test
  public void findUriPath() throws IOException {
    UrlWithProviderPrefixClientHttpRequest request =
        (UrlWithProviderPrefixClientHttpRequest) factory.createRequest(uri, HttpMethod.GET);

    Assertions.assertEquals("/v1/path", request.findUriPath(uri));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void invoke_checkPath() {
    Invocation invocation = Mockito.mock(Invocation.class);
    RequestMeta requestMeta = Mockito.mock(RequestMeta.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(requestMeta.getOperationMeta()).thenReturn(operationMeta);
    Map<String, Object> handlerContext = new HashMap<>();
    UrlWithProviderPrefixClientHttpRequest request = new UrlWithProviderPrefixClientHttpRequest(uri, HttpMethod.GET,
        "/a/b/c") {
      @Override
      protected Response doInvoke(Invocation invocation) {
        return Response.ok(null);
      }
    };

    Mockito.when(invocation.getHandlerContext()).thenReturn(handlerContext);
    try (MockedStatic<InvocationFactory> invocationFactoryMockedStatic = Mockito.mockStatic(InvocationFactory.class)) {
      invocationFactoryMockedStatic.when(() -> InvocationFactory.forConsumer(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
              .thenReturn(invocation);
      request.setRequestMeta(requestMeta);
      request.setPath(request.findUriPath(uri));

      request.invoke(new HashMap<>());

      Assertions.assertEquals("/v1/path", handlerContext.get(RestConst.REST_CLIENT_REQUEST_PATH));
    }
  }
}
