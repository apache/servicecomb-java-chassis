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
package org.apache.servicecomb.common.rest.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Mocked;

public class TestHttpServerFilterBeforeSendResponseExecutor {
  @Mocked
  Invocation invocation;

  @Mocked
  HttpServletResponseEx responseEx;

  List<HttpServerFilter> httpServerFilters = new ArrayList<>();

  HttpServerFilterBeforeSendResponseExecutor executor =
      new HttpServerFilterBeforeSendResponseExecutor(httpServerFilters, invocation, responseEx);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    httpServerFilters.add(new HttpServerFilterBaseForTest());
  }

  @Test
  public void runSucc() throws InterruptedException, ExecutionException {
    CompletableFuture<Void> result = executor.run();

    Assert.assertNull(result.get());
  }

  @Test
  public void runFail() throws InterruptedException, ExecutionException {
    httpServerFilters.add(new HttpServerFilterBaseForTest() {
      @Override
      public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
        throw new Error("");
      }
    });

    CompletableFuture<Void> result = executor.run();

    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(Matchers.instanceOf(Error.class));

    result.get();
  }
}
