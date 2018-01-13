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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.exception.CseException;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestFallbackPolicyManager {
  @Test
  public void testFallbackPolicyManager(final @Mocked Configuration config, final @Mocked Invocation invocation,
      final @Mocked OperationMeta operation) {
    FallbackPolicyManager.addPolicy(new ReturnNullFallbackPolicy());
    FallbackPolicyManager.addPolicy(new ThrowExceptionFallbackPolicy());
    FallbackPolicyManager.addPolicy(new FromCacheFallbackPolicy());

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

    Assert.assertEquals((String) null, FallbackPolicyManager.getFallbackResponse("Consumer", invocation).getResult());

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
    Assert.assertEquals(CseException.class,
        ((Exception) FallbackPolicyManager.getFallbackResponse("Consumer", invocation).getResult()).getCause()
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
    Assert.assertEquals("mockedsuccess",
        FallbackPolicyManager.getFallbackResponse("Consumer", invocation).getResult());

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
    Assert.assertEquals(CseException.class,
        ((Exception) FallbackPolicyManager.getFallbackResponse("Consumer", invocation).getResult()).getCause()
            .getClass());
  }
}
