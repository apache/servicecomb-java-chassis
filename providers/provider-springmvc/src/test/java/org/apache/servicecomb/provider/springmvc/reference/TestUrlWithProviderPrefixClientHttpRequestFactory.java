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
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.provider.springmvc.reference.UrlWithProviderPrefixClientHttpRequestFactory.UrlWithProviderPrefixClientHttpRequest;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestUrlWithProviderPrefixClientHttpRequestFactory {
  UrlWithProviderPrefixClientHttpRequestFactory factory = new UrlWithProviderPrefixClientHttpRequestFactory("/a/b/c");

  URI uri = URI.create("cse://ms/a/b/c/v1/path");

  @Test
  public void findUriPath() throws IOException {
    UrlWithProviderPrefixClientHttpRequest request =
        (UrlWithProviderPrefixClientHttpRequest) factory.createRequest(uri, HttpMethod.GET);

    Assert.assertEquals("/v1/path", request.findUriPath(uri));
  }

  @Test
  public void invoke_checkPath(@Mocked Invocation invocation, @Mocked RequestMeta requestMeta) {
    Map<String, String> handlerContext = new HashMap<>();
    UrlWithProviderPrefixClientHttpRequest request = new UrlWithProviderPrefixClientHttpRequest(uri, HttpMethod.GET,
        "/a/b/c") {
      @Override
      protected Response doInvoke(Invocation invocation) {
        return Response.ok(null);
      }
    };

    new Expectations(InvocationFactory.class) {
      {
        invocation.getHandlerContext();
        result = handlerContext;
        InvocationFactory.forConsumer((ReferenceConfig) any, (OperationMeta) any, (Object[]) any);
        result = invocation;
      }
    };

    Deencapsulation.setField(request, "requestMeta", requestMeta);
    Deencapsulation.setField(request, "path", request.findUriPath(uri));

    Deencapsulation.invoke(request, "invoke", new Object[] {new Object[] {}});

    Assert.assertEquals("/v1/path", handlerContext.get(RestConst.REST_CLIENT_REQUEST_PATH));
  }
}
