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

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.Part;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.definition.RestMetaUtils;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestServerRestArgsFilter {
  @Mocked
  Invocation invocation;

  @Mocked
  HttpServletResponseEx responseEx;

  @Mocked
  Response response;

  @Mocked
  Part part;

  @Mocked
  OperationMeta operationMeta;

  boolean invokedSendPart;

  ServerRestArgsFilter filter = new ServerRestArgsFilter();

  @Test
  public void asyncBeforeSendResponse_part(@Mocked RestOperationMeta restOperationMeta) {
    ResponsesMeta responsesMeta = new ResponsesMeta();
    responsesMeta.getResponseMap().put(202, RestObjectMapperFactory.getRestObjectMapper().constructType(Part.class));
    new Expectations(RestMetaUtils.class) {
      {
        responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE);
        result = response;
        response.getResult();
        result = part;
        response.getStatusCode();
        result = 202;
        invocation.findResponseType(202);
        result = TypeFactory.defaultInstance().constructType(Part.class);
      }
    };
    new MockUp<HttpServletResponseEx>(responseEx) {
      @Mock
      CompletableFuture<Void> sendPart(Part body) {
        invokedSendPart = true;
        return null;
      }
    };

    Assertions.assertNull(filter.beforeSendResponseAsync(invocation, responseEx));
    Assertions.assertTrue(invokedSendPart);
  }
}
