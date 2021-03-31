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

package org.apache.servicecomb.demo.springmvc.client;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.invocation.timeout.ProcessingTimeStrategy;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.CommonSchemaInterface;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

@Component
public class TestSpringMVCCommonSchemaInterface implements CategorizedTestCase {
  @RpcReference(schemaId = "SpringMVCCommonSchemaInterface", microserviceName = "springmvc")
  private CommonSchemaInterface client;

  @Override
  public void testAllTransport() throws Exception {
    testInvocationTimeoutInServer();
    testInvocationTimeoutInServerUserCheck();
    testInvocationAlreadyTimeoutInClient();
  }

  private void testInvocationTimeoutInServerUserCheck() {
    try {
      InvocationContext context = new InvocationContext();
      client.testInvocationTimeout(context, 1001, "customized");
      TestMgr.fail("should timeout");
    } catch (InvocationException e) {
      TestMgr.check(408, e.getStatusCode());
      TestMgr.check("Invocation Timeout.", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }

  private void testInvocationAlreadyTimeoutInClient() {
    try {
      InvocationContext context = new InvocationContext();
      context.addLocalContext(ProcessingTimeStrategy.CHAIN_START_TIME, System.nanoTime());
      context.addLocalContext(ProcessingTimeStrategy.CHAIN_PROCESSING, TimeUnit.SECONDS.toNanos(1));
      client.testInvocationTimeout(context, 1, "hello");
      TestMgr.fail("should timeout");
    } catch (InvocationException e) {
      TestMgr.check(408, e.getStatusCode());
      TestMgr.check("Invocation Timeout.", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }

  private void testInvocationTimeoutInServer() {
    try {
      client.testInvocationTimeout(1001, "hello");
      TestMgr.fail("should timeout");
    } catch (InvocationException e) {
      TestMgr.check(408, e.getStatusCode());
      TestMgr.check("Invocation Timeout.", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
  }
}
