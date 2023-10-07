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

package org.apache.servicecomb.faultinjection;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestConsumerAbortFaultFilter {
  private Invocation invocation;

  private Environment environment;

  @BeforeEach
  public void before() {
    environment = Mockito.mock(Environment.class);
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty(
        "servicecomb.governance.Consumer.carts6.schemas.testSchema4.operations.sayBye4"
            + ".policy.fault.protocols.rest.abort.percent",
        int.class, -1)).thenReturn(-1);
    Mockito.when(environment.getProperty(
        "servicecomb.governance.Consumer.carts6.schemas.testSchema4.operations.sayBye4"
            + ".policy.fault.protocols.rest.abort.httpStatus",
        int.class, -1)).thenReturn(-1);

    Mockito.when(environment.getProperty(
        "servicecomb.governance.Consumer.carts6.schemas.testSchema4"
            + ".policy.fault.protocols.rest.abort.percent",
        int.class, -1)).thenReturn(-1);
    Mockito.when(environment.getProperty(
        "servicecomb.governance.Consumer.carts6.schemas.testSchema4"
            + ".policy.fault.protocols.rest.abort.httpStatus",
        int.class, -1)).thenReturn(-1);

    Mockito.when(environment.getProperty(
        "servicecomb.governance.Consumer.carts6"
            + ".policy.fault.protocols.rest.abort.percent",
        int.class, -1)).thenReturn(-1);
    Mockito.when(environment.getProperty(
        "servicecomb.governance.Consumer.carts6"
            + ".policy.fault.protocols.rest.abort.httpStatus",
        int.class, -1)).thenReturn(-1);

    Mockito.when(environment.getProperty(
            "servicecomb.governance.Consumer._global"
                + ".policy.fault.protocols.rest.abort.percent",
            int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(
            "servicecomb.governance.Consumer._global"
                + ".policy.fault.protocols.rest.abort.httpStatus",
            int.class, -1))
        .thenReturn(-1);

    FaultInjectionUtil.getConfigCenterValue().clear();

    invocation = Mockito.mock(Invocation.class);
    Transport transport = Mockito.mock(Transport.class);
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName12");
    Mockito.when(invocation.getTransport()).thenReturn(transport);
    Mockito.when(transport.getName()).thenReturn("rest");
    Mockito.when(invocation.getOperationName()).thenReturn("sayBye4");
    Mockito.when(invocation.getSchemaId()).thenReturn("testSchema4");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("carts6");
  }

  @AfterEach
  public void after() {
  }

  @AfterAll
  public static void classTeardown() {
    VertxUtils.blockCloseVertxByName("faultinjectionTest");
  }

  @Test
  public void injectFaultError() {
    Mockito.when(environment.getProperty(
            "servicecomb.governance.Consumer._global"
                + ".policy.fault.protocols.rest.abort.httpStatus",
            int.class, -1))
        .thenReturn(421);
    Mockito.when(environment.getProperty(
            "servicecomb.governance.Consumer._global"
                + ".policy.fault.protocols.rest.abort.percent",
            int.class, -1))
        .thenReturn(100);

    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();

    FilterNode filterNode = Mockito.mock(FilterNode.class);

    Holder<InvocationException> resultHolder = new Holder<>();
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    result.whenComplete((r, e) -> {
      if (e != null) {
        resultHolder.value = (InvocationException) e;
      }
    });
    Assertions.assertThrows(ExecutionException.class, () -> result.get());
    Assertions.assertEquals(421, resultHolder.value.getStatusCode());
    Assertions.assertEquals("aborted by fault inject", resultHolder.value.getReasonPhrase());
    Assertions.assertEquals("CommonExceptionData [message=aborted by fault inject]",
        resultHolder.value.getErrorData().toString());
  }

  @Test
  public void injectFaultNoError() throws Exception {
    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();
    FilterNode filterNode = Mockito.mock(FilterNode.class);
    Mockito.when(filterNode.onFilter(invocation))
        .thenReturn(CompletableFuture.completedFuture(Response.succResp("success")));
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    Assertions.assertEquals("success", result.get().getResult());
  }

  @Test
  public void injectFaultNoPercentageConfig() throws Exception {
    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();
    FilterNode filterNode = Mockito.mock(FilterNode.class);
    Mockito.when(filterNode.onFilter(invocation))
        .thenReturn(CompletableFuture.completedFuture(Response.succResp("success")));
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    Assertions.assertEquals("success", result.get().getResult());
  }

  @Test
  public void injectFaultNoErrorCodeConfig() throws Exception {
    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();
    FilterNode filterNode = Mockito.mock(FilterNode.class);
    Mockito.when(filterNode.onFilter(invocation))
        .thenReturn(CompletableFuture.completedFuture(Response.succResp("success")));
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    Assertions.assertEquals("success", result.get().getResult());
  }
}
