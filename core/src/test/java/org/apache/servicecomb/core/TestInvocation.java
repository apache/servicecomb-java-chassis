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

import javax.xml.ws.Holder;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

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

  static long currentNanoTime = 123;

  @BeforeClass
  public static void classSetup() {
    EventManager.eventBus = new EventBus();
  }

  protected static void mockNonaTime() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return currentNanoTime;
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
    invocation.onStart();

    Assert.assertEquals(currentNanoTime, result.value.getStartTime());
    Assert.assertSame(invocation, result.value);

    EventManager.unregister(subscriber);
  }

  @Test
  public void onStartExecute() {
    mockNonaTime();

    Invocation invocation = new Invocation(endpoint, operationMeta, swaggerArguments);
    invocation.onStartExecute();

    Assert.assertEquals(currentNanoTime, invocation.getStartExecutionTime());
  }

  @Test
  public void onFinish(@Mocked Response response) {
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
    invocation.onFinish(response);

    Assert.assertEquals(currentNanoTime, result.value.getNanoCurrent());
    Assert.assertSame(invocation, result.value.getInvocation());
    Assert.assertSame(response, result.value.getResponse());

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
}
