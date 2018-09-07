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

package org.apache.servicecomb.core.provider.consumer;

import java.util.Arrays;

import javax.xml.ws.Holder;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvokerUtils {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mocked
  ReferenceConfig referenceConfig;

  @Mocked
  SchemaMeta schemaMeta;

  @Mocked
  OperationMeta operationMeta;

  Invocation invocation;

  static Object invokeResult;

  SCBEngine scbEngine = new SCBEngine();

  static long nanoTime = 1;

  @BeforeClass
  public static void classSetup() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @Before
  public void setup() {
    new MockUp<SCBEngine>() {
      @Mock
      SCBEngine getInstance() {
        return scbEngine;
      }
    };
    scbEngine.setStatus(SCBStatus.UP);

    new Expectations() {
      {
        operationMeta.getSchemaMeta();
        result = schemaMeta;
        schemaMeta.getConsumerHandlerChain();
        result = Arrays.asList((Handler) (i, ar) -> {
          System.out.println(invokeResult);
          ar.success(invokeResult);
        });
      }
    };
    invocation = new Invocation(referenceConfig, operationMeta, new Object[] {});
  }

  @Test
  public void testSyncInvokeInvocationWithException() {
    Invocation invocation = Mockito.mock(Invocation.class);
    InvocationStageTrace stageTrace = new InvocationStageTrace(invocation);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(stageTrace);

    Response response = Mockito.mock(Response.class);
    new MockUp<SyncResponseExecutor>() {
      @Mock
      public Response waitResponse() {
        return response;
      }
    };
    Mockito.when(response.isSuccessed()).thenReturn(false);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn("test");

    expectedException.expect(InvocationException.class);
    expectedException.expect(Matchers.hasProperty("statusCode", Matchers.is(490)));
    InvokerUtils.syncInvoke(invocation);
  }

  @Test
  public void testSyncInvokeNormal() {
    invokeResult = 1;
    Assert.assertEquals(1, (int) InvokerUtils.syncInvoke(invocation));
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getStart());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getStartHandlersRequest());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getFinishHandlersResponse());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getFinish());
  }

  @Test
  public void testReactiveInvoke(@Mocked InvocationContext parentContext, @Mocked Response response) {
    new MockUp<Invocation>(invocation) {
      @Mock
      InvocationContext getParentContext() {
        return parentContext;
      }

      @Mock
      void next(AsyncResponse asyncResp) {
        asyncResp.handle(response);
      }
    };

    Holder<InvocationContext> holder = new Holder<>();
    InvokerUtils.reactiveInvoke(invocation, ar -> holder.value = ContextUtils.getInvocationContext());

    Assert.assertNull(ContextUtils.getInvocationContext());
    Assert.assertSame(parentContext, holder.value);
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getStart());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getStartHandlersRequest());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getFinishHandlersResponse());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getFinish());
  }

  @Test
  public void reactiveInvokeException() {
    new MockUp<Invocation>(invocation) {
      @Mock
      void next(AsyncResponse asyncResp) {
        throw new Error();
      }
    };

    Holder<Response> holder = new Holder<>();
    InvokerUtils.reactiveInvoke(invocation, ar -> holder.value = ar);

    Assert.assertFalse(holder.value.isSuccessed());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getStart());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getStartHandlersRequest());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getFinishHandlersResponse());
    Assert.assertEquals(1, invocation.getInvocationStageTrace().getFinish());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void invoke() {
    new MockUp<InvokerUtils>() {
      @Mock
      Object syncInvoke(Invocation invocation) {
        return 1;
      }
    };

    Assert.assertEquals(1, InvokerUtils.invoke(null));
  }

  @Test
  public void testSyncInvoke_4param_NotReady() {
    scbEngine.setStatus(SCBStatus.DOWN);

    expectedException.expect(InvocationException.class);
    expectedException.expectMessage(
        Matchers
            .is("InvocationException: code=503;msg=CommonExceptionData [message=The request is rejected. Cannot process the request due to STATUS = DOWN]"));
    InvokerUtils.syncInvoke("ms", "schemaId", "opName", null);
  }

  @Test
  public void testSyncInvoke_6param_NotReady() {
    scbEngine.setStatus(SCBStatus.DOWN);

    expectedException.expect(InvocationException.class);
    expectedException.expectMessage(
        Matchers
            .is("InvocationException: code=503;msg=CommonExceptionData [message=The request is rejected. Cannot process the request due to STATUS = DOWN]"));

    InvokerUtils.syncInvoke("ms", "latest", "rest", "schemaId", "opName", null);
  }

  @Test
  public void testSyncInvokeReady(@Injectable ConsumerProviderManager consumerProviderManager,
      @Injectable Invocation invocation) {
    scbEngine.setConsumerProviderManager(consumerProviderManager);

    new Expectations(InvocationFactory.class) {
      {
        InvocationFactory.forConsumer((ReferenceConfig) any, (SchemaMeta) any, (String) any, (Object[]) any);
        result = invocation;
      }
    };
    new Expectations(InvokerUtils.class) {
      {
        InvokerUtils.syncInvoke(invocation);
        result = "ok";
      }
    };
    Object result1 = InvokerUtils.syncInvoke("ms", "schemaId", "opName", null);
    Assert.assertEquals("ok", result1);

    Object result2 = InvokerUtils.syncInvoke("ms", "latest", "rest", "schemaId", "opName", null);
    Assert.assertEquals("ok", result2);

    CseContext.getInstance().setConsumerProviderManager(null);
  }
}
