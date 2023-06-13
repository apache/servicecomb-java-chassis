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
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.netflix.config.DynamicProperty;

public class TestConsumerAbortFaultFilter {
  private Invocation invocation;

  @BeforeEach
  public void before() {
    ArchaiusUtils.resetConfig();
    FaultInjectionConfig.getCfgCallback().clear();
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
    ArchaiusUtils.resetConfig();
  }

  @AfterAll
  public static void classTeardown() {
    VertxUtils.blockCloseVertxByName("faultinjectionTest");
  }

  @Test
  public void injectFaultError() {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.httpStatus", "421");
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent", "100");

    Assertions.assertEquals("421", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.httpStatus")
        .getString());
    Assertions.assertEquals("100", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent")
        .getString());

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
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.httpStatus", "421");
    ArchaiusUtils.setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent", "0");

    Assertions.assertEquals("421", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.httpStatus")
        .getString());
    Assertions.assertEquals("0", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent")
        .getString());

    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();
    FilterNode filterNode = Mockito.mock(FilterNode.class);
    Mockito.when(filterNode.onFilter(invocation))
        .thenReturn(CompletableFuture.completedFuture(Response.succResp("success")));
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    Assertions.assertEquals("success", result.get().getResult());
  }

  @Test
  public void injectFaultNoPercentageConfig() throws Exception {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent", null);

    Assertions.assertNull(DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent")
        .getString());

    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();
    FilterNode filterNode = Mockito.mock(FilterNode.class);
    Mockito.when(filterNode.onFilter(invocation))
        .thenReturn(CompletableFuture.completedFuture(Response.succResp("success")));
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    Assertions.assertEquals("success", result.get().getResult());
  }

  @Test
  public void injectFaultNoErrorCodeConfig() throws Exception {
    ArchaiusUtils
        .setProperty("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent", "10");

    Assertions.assertEquals("10", DynamicProperty
        .getInstance("servicecomb.governance.Consumer._global.policy.fault.protocols.rest.abort.percent")
        .getString());

    ConsumerAbortFaultFilter abortFault = new ConsumerAbortFaultFilter();
    FilterNode filterNode = Mockito.mock(FilterNode.class);
    Mockito.when(filterNode.onFilter(invocation))
        .thenReturn(CompletableFuture.completedFuture(Response.succResp("success")));
    CompletableFuture<Response> result = abortFault.onFilter(invocation, filterNode);
    Assertions.assertEquals("success", result.get().getResult());
  }
}
