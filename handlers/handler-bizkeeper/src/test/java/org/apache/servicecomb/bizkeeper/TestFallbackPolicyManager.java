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
package org.apache.servicecomb.bizkeeper;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.exception.CseException;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestFallbackPolicyManager {
  @Test
  public void testFallbackPolicyManager() {
    OperationMeta operation = Mockito.mock(OperationMeta.class);
    Invocation invocation = Mockito.mock(Invocation.class);

    FallbackPolicyManager.addPolicy(new ReturnNullFallbackPolicy());
    FallbackPolicyManager.addPolicy(new ThrowExceptionFallbackPolicy());
    FallbackPolicyManager.addPolicy(new FromCacheFallbackPolicy());
    FallbackPolicyManager.addPolicy(new FallbackPolicy() {
      private static final String CUSTOM = "custom";

      @Override
      public String name() {
        return CUSTOM;
      }

      @Override
      public Response getFallbackResponse(Invocation invocation, Throwable error) {
        if (error instanceof InvocationException) {
          return Response.succResp("test");
        }
        if (error instanceof RuntimeException) {
          return Response.succResp("runtime");
        }
        return null;
      }
    });

    Mockito.when(operation.getMicroserviceQualifiedName()).thenReturn("testservice.schema.custom");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testservice");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operation);
    ArchaiusUtils.setProperty("servicecomb.fallbackpolicy.Consumer.testservice.schema.custom.policy", "custom");
    Assertions.assertEquals("runtime",
        FallbackPolicyManager.getFallbackResponse("Consumer", new RuntimeException(), invocation)
            .getResult());

    Mockito.when(operation.getMicroserviceQualifiedName()).thenReturn("testservice.schema.returnnull");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testservice");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operation);
    ArchaiusUtils.setProperty("servicecomb.fallbackpolicy.Consumer.testservice.schema.returnnull.policy", "returnnull");
    Assertions.assertEquals((String) null,
        FallbackPolicyManager.getFallbackResponse("Consumer", null, invocation).getResult());

    Mockito.when(operation.getMicroserviceQualifiedName()).thenReturn("testservice.schema.throwexception");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testservice");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operation);
    ArchaiusUtils.setProperty("servicecomb.fallbackpolicy.Consumer.testservice.schema.throwexception.policy", "throwexception");
    Assertions.assertEquals(CseException.class,
        ((Exception) FallbackPolicyManager.getFallbackResponse("Consumer", null, invocation).getResult()).getCause()
            .getClass());

    Mockito.when(operation.getMicroserviceQualifiedName()).thenReturn("testservice.schema.fromcache");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testservice");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operation);
    ArchaiusUtils.setProperty("servicecomb.fallbackpolicy.Consumer.testservice.schema.fromcache.policy", "fromcache");
    Mockito.when(invocation.getInvocationQualifiedName()).thenReturn("estservice.schema.fromcache");

    FallbackPolicyManager.record("Consumer", invocation, Response.succResp("mockedsuccess"), true);
    FallbackPolicyManager.record("Consumer", invocation, Response.succResp("mockedfailure"), false);
    Assertions.assertEquals("mockedsuccess",
        FallbackPolicyManager.getFallbackResponse("Consumer", null, invocation).getResult());

    Mockito.when(operation.getMicroserviceQualifiedName()).thenReturn("testservice.schema.unknown");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testservice");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operation);
    ArchaiusUtils.setProperty("servicecomb.fallbackpolicy.Consumer.testservice.schema.unknown.policy", "unknown");
    Assertions.assertEquals(InvocationException.class,
        ((Exception) FallbackPolicyManager.getFallbackResponse("Consumer", new InvocationException(
            Status.TOO_MANY_REQUESTS, ""), invocation).getResult()).getClass());

    ArchaiusUtils.resetConfig();
  }
}
