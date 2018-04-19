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
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

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

  boolean invokedSendPart;

  ServerRestArgsFilter filter = new ServerRestArgsFilter();

  @Test
  public void asyncBeforeSendResponse_part() {
    new Expectations() {
      {
        responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE);
        result = response;
        response.getResult();
        result = part;
      }
    };
    new MockUp<HttpServletResponseEx>(responseEx) {
      @Mock
      CompletableFuture<Void> sendPart(Part body) {
        invokedSendPart = true;
        return null;
      }
    };

    Assert.assertNull(filter.beforeSendResponseAsync(invocation, responseEx));
    Assert.assertTrue(invokedSendPart);
  }
}
