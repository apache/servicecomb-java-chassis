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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestConfig {
  @Test
  public void testConstants() {
    Assertions.assertEquals("x-cse-context", Const.CSE_CONTEXT);
    Assertions.assertEquals("rest", Const.RESTFUL);
    Assertions.assertEquals("", Const.ANY_TRANSPORT);
    Assertions.assertEquals("latest", Const.VERSION_RULE_LATEST);
    Assertions.assertEquals("0.0.0.0+", Const.DEFAULT_VERSION_RULE);
  }

  @Test
  public void testHttpResponse() {
    String objectString = new String("Unit Testing");
    Response oResponse = Response.success(objectString, Status.OK);

    Assertions.assertTrue(oResponse.isSucceed());

    oResponse = Response.succResp(objectString);
    Assertions.assertTrue(oResponse.isSucceed());
    Assertions.assertEquals(200, oResponse.getStatusCode());

    Throwable oThrowable = new Throwable("Error");

    oResponse = Response.consumerFailResp(oThrowable);
    Assertions.assertTrue(oResponse.isFailed());

    oResponse = Response.providerFailResp(oThrowable);
    Assertions.assertTrue(oResponse.isFailed());
  }

  @Test
  public void testHttpStatus() {
    StatusType oStatus = new HttpStatus(204, "InternalServerError");
    Assertions.assertEquals("InternalServerError", oStatus.getReasonPhrase());
  }

  @Test
  public void testContextUtils() {
    ThreadLocal<InvocationContext> contextMgr = new ThreadLocal<>();
    Assertions.assertEquals(contextMgr.get(), ContextUtils.getInvocationContext());

    SwaggerInvocation invocation = new SwaggerInvocation();
    invocation.addContext("test1", "testObject");

    Assertions.assertEquals("testObject", invocation.getContext("test1"));

    Map<String, String> context = new HashMap<>();
    context.put("test2", new String("testObject"));
    invocation.setContext(context);
    Assertions.assertEquals(context, invocation.getContext());

    invocation.setStatus(Status.OK);
    Assertions.assertEquals(200, invocation.getStatus().getStatusCode());

    invocation.setStatus(204);
    Assertions.assertEquals(204, invocation.getStatus().getStatusCode());

    invocation.setStatus(Status.OK);
    Assertions.assertEquals((Status.OK).getStatusCode(), invocation.getStatus().getStatusCode());

    invocation.setStatus(203, "Done");
    Assertions.assertEquals(203, invocation.getStatus().getStatusCode());

    ContextUtils.setInvocationContext(invocation);
    Assertions.assertEquals(invocation, ContextUtils.getInvocationContext());

    ContextUtils.removeInvocationContext();
    Assertions.assertNull(ContextUtils.getInvocationContext());
  }

  @Test
  public void testResponse() {
    Response response = Response.create(400, "test", null);
    InvocationException exception = response.getResult();
    Assertions.assertNull(exception.getErrorData());

    response = Response.create(400, "test", "errorData");
    exception = response.getResult();
    Assertions.assertEquals("errorData", exception.getErrorData());
  }
}
