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
package org.apache.servicecomb.core;

import java.util.Arrays;

import javax.xml.ws.Holder;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.event.InvocationBaseEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodFinishEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodStartEvent;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.tracing.BraveTraceIdGenerator;
import org.apache.servicecomb.core.tracing.TraceIdGenerator;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvocation {
  Invocation invocation;

  @Mocked
  Endpoint endpoint;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  Object[] swaggerArguments;

  static long nanoTime = 123;

  @BeforeClass
  public static void classSetup() {
    EventManager.eventBus = new EventBus();
  }

  protected static void mockNonaTime() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @AfterClass
  public static void classTeardown() {
    EventManager.eventBus = new EventBus();
  }

  @Test
  public void onStart() {
    mockNonaTime();

    Holder<Invocation> result = new Holder<>();
    Object subscriber = new Object() {
      @Subscribe
      public void onStart(InvocationStartEvent event) {
        result.value = event.getInvocation();
      }
    };
    EventManager.register(subscriber);

    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    invocation.onStart(nanoTime);

    Assert.assertSame(invocation, result.value);
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStart());

    EventManager.unregister(subscriber);
  }

  @Test
  public void onStartExecute() {
    mockNonaTime();

    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    invocation.onExecuteStart();

    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartExecution());
  }

  @Test
  public void onFinish() {
    mockNonaTime();

    Holder<InvocationFinishEvent> result = new Holder<>();
    Object subscriber = new Object() {
      @Subscribe
      public void onStart(InvocationFinishEvent event) {
        result.value = event;
      }
    };
    EventManager.register(subscriber);

    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    Assert.assertFalse(invocation.isFinished());
    Response response = Response.succResp(null);
    invocation.onFinish(response);

    Assert.assertEquals(nanoTime, result.value.getNanoCurrent());
    Assert.assertSame(invocation, result.value.getInvocation());
    Assert.assertSame(response, result.value.getResponse());
    Assert.assertTrue(invocation.isFinished());

    // should not post event again
    InvocationFinishEvent oldEvent = result.value;
    invocation.onFinish(null);
    Assert.assertSame(oldEvent, result.value);

    EventManager.unregister(subscriber);
  }

  @Test
  public void isConsumer_yes() {
    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    Assert.assertFalse(invocation.isConsumer());
  }

  @Test
  public void isConsumer_no(@Mocked ReferenceConfig referenceConfig) {
    Invocation invocation = new Invocation(referenceConfig, operationMeta, swaggerArguments);
    Assert.assertTrue(invocation.isConsumer());
  }

  @Test
  public void localContext(@Mocked ReferenceConfig referenceConfig) {
    Invocation invocation = new Invocation(referenceConfig, operationMeta, swaggerArguments);

    invocation.addLocalContext("k", 1);
    Assert.assertSame(invocation.getHandlerContext(), invocation.getLocalContext());
    Assert.assertEquals(1, (int) invocation.getLocalContext("k"));
  }

  @Test
  public void traceId_fromContext(@Mocked ReferenceConfig referenceConfig) {
    Invocation invocation = new Invocation(referenceConfig, operationMeta, swaggerArguments);
    invocation.addContext(Const.TRACE_ID_NAME, "abc");

    invocation.onStart(0);

    Assert.assertEquals("abc", invocation.getTraceId());
    Assert.assertEquals("abc", invocation.getTraceId(Const.TRACE_ID_NAME));
  }

  @Test
  public void traceId_consumerCreateTraceId(@Mocked ReferenceConfig referenceConfig) {
    TraceIdGenerator generator = SPIServiceUtils.getTargetService(TraceIdGenerator.class, BraveTraceIdGenerator.class);
    new Expectations(generator) {
      {
        generator.generate();
        result = "abc";
      }
    };
    Invocation invocation = new Invocation(referenceConfig, operationMeta, swaggerArguments);

    invocation.onStart(0);

    Assert.assertEquals("abc", invocation.getTraceId());
    Assert.assertEquals("abc", invocation.getTraceId(Const.TRACE_ID_NAME));
  }

  @Test
  public void traceId_fromRequest(@Mocked Endpoint endpoint, @Mocked HttpServletRequestEx requestEx) {
    new Expectations() {
      {
        requestEx.getHeader(Const.TRACE_ID_NAME);
        result = "abc";
      }
    };
    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);

    invocation.onStart(requestEx, 0);

    Assert.assertEquals("abc", invocation.getTraceId());
    Assert.assertEquals("abc", invocation.getTraceId(Const.TRACE_ID_NAME));
  }

  @Test
  public void traceId_producerCreateTraceId(@Mocked Endpoint endpoint, @Mocked HttpServletRequestEx requestEx) {
    TraceIdGenerator generator = SPIServiceUtils.getTargetService(TraceIdGenerator.class, BraveTraceIdGenerator.class);
    new Expectations(generator) {
      {
        generator.generate();
        result = "abc";
      }
    };
    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);

    invocation.onStart(requestEx, 0);

    Assert.assertEquals("abc", invocation.getTraceId());
    Assert.assertEquals("abc", invocation.getTraceId(Const.TRACE_ID_NAME));
  }

  @Test
  public void traceIdGeneratorInit(@Mocked TraceIdGenerator gen1, @Mocked TraceIdGenerator gen2,
      @Mocked TraceIdGenerator gen3, @Mocked TraceIdGenerator gen4) {
    new Expectations(SPIServiceUtils.class) {
      {
        gen1.getName();
        result = "zipkin";

        gen3.getName();
        result = "apm";

        gen2.getName();
        result = "zipkin";

        gen4.getName();
        result = "apm";

        SPIServiceUtils.getOrLoadSortedService(TraceIdGenerator.class);
        result = Arrays.asList(gen1, gen3, gen2, gen4);
      }
    };

    Assert.assertThat(Invocation.loadTraceIdGenerators(), Matchers.contains(gen1, gen3));
  }

  InvocationBaseEvent invocationBaseEvent;

  @Test
  public void onBusinessMethodStart() {
    Object listener = new Object() {
      @Subscribe
      public void onBusinessMethodStart(InvocationBusinessMethodStartEvent event) {
        invocationBaseEvent = event;
      }
    };
    EventManager.getEventBus().register(listener);
    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    mockNonaTime();
    invocation.onBusinessMethodStart();
    EventManager.getEventBus().unregister(listener);

    Assert.assertSame(invocation, invocationBaseEvent.getInvocation());
    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getStartBusinessMethod());
  }

  @Test
  public void onBusinessMethodFinish() {
    Object listener = new Object() {
      @Subscribe
      public void onBusinessMethodStart(InvocationBusinessMethodFinishEvent event) {
        invocationBaseEvent = event;
      }
    };
    EventManager.getEventBus().register(listener);
    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    invocation.onBusinessMethodFinish();
    EventManager.getEventBus().unregister(listener);

    Assert.assertSame(invocation, invocationBaseEvent.getInvocation());
  }

  @Test
  public void onBusinessFinish() {
    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    mockNonaTime();
    invocation.onBusinessFinish();

    Assert.assertEquals(nanoTime, invocation.getInvocationStageTrace().getFinishBusiness());
  }
}
