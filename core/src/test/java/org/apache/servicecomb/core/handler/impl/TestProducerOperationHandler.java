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

package org.apache.servicecomb.core.handler.impl;

import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.junit.Test;

import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

// test cases copied from delete class, this test case tests ExceptionToProducerResponseConverter
public class TestProducerOperationHandler {
  ProducerOperationHandler handler = new ProducerOperationHandler();

  @Test
  public void processException_normal(@Mocked SwaggerInvocation invocation) {
    Error error = new Error("abc");

    Response response = handler.processException(invocation, error);
    Assertions.assertSame(Status.OK, response.getStatus());
    Assertions.assertEquals("response from error: abc", response.getResult());
  }

  @Test
  public void processException_InvocationTargetException(@Mocked SwaggerInvocation invocation) {
    Error error = new Error("abc");
    InvocationTargetException targetException = new InvocationTargetException(error);

    Response response = handler.processException(invocation, error);
    Assertions.assertSame(Status.OK, response.getStatus());
    Assertions.assertEquals("response from error: abc", response.getResult());
  }
}
