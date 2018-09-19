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
package org.apache.servicecomb.it.testcase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTrace {
  interface TraceSchemaIntf {
    CompletableFuture<String> echoProxy();
  }

  private static Consumers<TraceSchemaIntf> consumers;


  private static String producerName;

  @Before
  public void prepare() {
    if (!ITJUnitUtils.getProducerName().equals(producerName)) {
      producerName = ITJUnitUtils.getProducerName();
      consumers = new Consumers<>(producerName, "trace", TraceSchemaIntf.class);
      consumers.init(ITJUnitUtils.getTransport());
    }

    InvocationContext context = new InvocationContext();
    context.addContext(Const.TRACE_ID_NAME, "testId");
    ContextUtils.setInvocationContext(context);
  }

  @After
  public void teardown() {
    ContextUtils.removeInvocationContext();
  }

  @Test
  public void echo_intf() throws ExecutionException, InterruptedException {
    String traceId = consumers.getIntf().echoProxy().get();
    Assert.assertEquals("testId", traceId);
  }

  @Test
  public void echo_rt() {
    String traceId = consumers.getSCBRestTemplate().getForObject("/echo-proxy", String.class);
    Assert.assertEquals("testId", traceId);
  }
}
