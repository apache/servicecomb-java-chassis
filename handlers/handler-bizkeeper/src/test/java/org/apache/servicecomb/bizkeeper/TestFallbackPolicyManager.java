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
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestFallbackPolicyManager {
  @Test
  public void testFallbackPolicyManager(final @Mocked Configuration config, final @Mocked Invocation invocation,
      final @Mocked OperationMeta operation) {
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

    new Expectations() {
      {
        invocation.getMicroserviceName();
        result = "testservice";
        invocation.getOperationMeta();
        result = operation;
        operation.getMicroserviceQualifiedName();
        result = "testservice.schema.custom";
        config.getFallbackPolicyPolicy("Consumer", "testservice", "testservice.schema.custom");
        result = "custom";
      }
    };

    Assertions.assertEquals("runtime",
        FallbackPolicyManager.getFallbackResponse("Consumer", new RuntimeException(), invocation)
            .getResult());

    new Expectations() {
      {
        invocation.getMicroserviceName();
        result = "testservice";
        invocation.getOperationMeta();
        result = operation;
        operation.getMicroserviceQualifiedName();
        result = "testservice.schema.returnnull";
        config.getFallbackPolicyPolicy("Consumer", "testservice", "testservice.schema.returnnull");
        result = "returnnull";
      }
    };

    Assertions.assertEquals((String) null,
        FallbackPolicyManager.getFallbackResponse("Consumer", null, invocation).getResult());

    new Expectations() {
      {
        invocation.getMicroserviceName();
        result = "testservice";
        invocation.getOperationMeta();
        result = operation;
        operation.getMicroserviceQualifiedName();
        result = "testservice.schema.throwexception";
        config.getFallbackPolicyPolicy("Consumer", "testservice", "testservice.schema.throwexception");
        result = "throwexception";
      }
    };
    Assertions.assertEquals(CseException.class,
        ((Exception) FallbackPolicyManager.getFallbackResponse("Consumer", null, invocation).getResult()).getCause()
            .getClass());

    new Expectations() {
      {
        invocation.getMicroserviceName();
        result = "testservice";
        invocation.getOperationMeta();
        result = operation;
        operation.getMicroserviceQualifiedName();
        result = "testservice.schema.fromcache";
        config.getFallbackPolicyPolicy("Consumer", "testservice", "testservice.schema.fromcache");
        result = "fromcache";
        invocation.getInvocationQualifiedName();
        result = "testservice.schema.fromcache";
      }
    };
    FallbackPolicyManager.record("Consumer", invocation, Response.succResp("mockedsuccess"), true);
    FallbackPolicyManager.record("Consumer", invocation, Response.succResp("mockedfailure"), false);
    Assertions.assertEquals("mockedsuccess",
        FallbackPolicyManager.getFallbackResponse("Consumer", null, invocation).getResult());

    new Expectations() {
      {
        invocation.getMicroserviceName();
        result = "testservice";
        invocation.getOperationMeta();
        result = operation;
        operation.getMicroserviceQualifiedName();
        result = "testservice.schema.unknown";
        config.getFallbackPolicyPolicy("Consumer", "testservice", "testservice.schema.unknown");
        result = "unknown";
      }
    };
    Assertions.assertEquals(InvocationException.class,
        ((Exception) FallbackPolicyManager.getFallbackResponse("Consumer", new InvocationException(
            Status.TOO_MANY_REQUESTS, ""), invocation).getResult()).getClass());
  }
}
