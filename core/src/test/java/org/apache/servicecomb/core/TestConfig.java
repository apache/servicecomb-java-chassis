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
import java.util.Properties;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.config.ConfigurationSpringInitializer;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Assert;
import org.junit.Test;

public class TestConfig {
  class MyConfigurationSpringInitializer extends ConfigurationSpringInitializer {
    Properties p;

    MyConfigurationSpringInitializer(Properties p) {
      this.p = p;
    }

    public void test() throws Exception {
      this.loadProperties(p);
    }
  }

  @Test
  public void testConstants() {
    Assert.assertEquals("x-cse-context", Const.CSE_CONTEXT);
    Assert.assertEquals("rest", Const.RESTFUL);
    Assert.assertEquals("", Const.ANY_TRANSPORT);
    Assert.assertEquals("latest", Const.VERSION_RULE_LATEST);
    Assert.assertEquals("0.0.0+", Const.DEFAULT_VERSION_RULE);
  }

  @Test
  public void testHttpResponse() {
    String objectString = new String("Unit Testing");
    Response oResponse = Response.success(objectString, Status.OK);

    Assert.assertEquals(true, oResponse.isSuccessed());

    oResponse = Response.succResp(objectString);
    Assert.assertEquals(true, oResponse.isSuccessed());
    Assert.assertEquals(200, oResponse.getStatusCode());

    Throwable oThrowable = new Throwable("Error");

    oResponse = Response.consumerFailResp(oThrowable);
    Assert.assertEquals(true, oResponse.isFailed());

    oResponse = Response.providerFailResp(oThrowable);
    Assert.assertEquals(true, oResponse.isFailed());
  }

  @Test
  public void testHttpStatus() {
    StatusType oStatus = new HttpStatus(204, "InternalServerError");
    Assert.assertEquals("InternalServerError", oStatus.getReasonPhrase());
  }

  @Test
  public void testContextUtils() {
    ThreadLocal<InvocationContext> contextMgr = new ThreadLocal<>();
    Assert.assertEquals(contextMgr.get(), ContextUtils.getInvocationContext());

    SwaggerInvocation invocation = new SwaggerInvocation();
    invocation.addContext("test1", "testObject");

    Assert.assertEquals("testObject", invocation.getContext("test1"));

    Map<String, String> context = new HashMap<>();
    context.put("test2", new String("testObject"));
    invocation.setContext(context);
    Assert.assertEquals(context, invocation.getContext());

    invocation.setStatus(Status.OK);
    Assert.assertEquals(200, invocation.getStatus().getStatusCode());

    invocation.setStatus(204);
    Assert.assertEquals(204, invocation.getStatus().getStatusCode());

    invocation.setStatus(Status.OK);
    Assert.assertEquals((Status.OK).getStatusCode(), invocation.getStatus().getStatusCode());

    invocation.setStatus(203, "Done");
    Assert.assertEquals(203, invocation.getStatus().getStatusCode());

    ContextUtils.setInvocationContext(invocation);
    Assert.assertEquals(invocation, ContextUtils.getInvocationContext());

    ContextUtils.removeInvocationContext();
    Assert.assertEquals(null, ContextUtils.getInvocationContext());
  }

  @Test
  public void testResponse() {
    Response response = Response.create(400, "test", null);
    InvocationException exception = response.getResult();
    Assert.assertEquals(null, exception.getErrorData());

    response = Response.create(400, "test", "errorData");
    exception = response.getResult();
    Assert.assertEquals("errorData", exception.getErrorData());
  }

  @Test
  public void testConfigurationSpringInitializer() throws Exception {
    Properties p = new Properties();
    MyConfigurationSpringInitializer oConf = new MyConfigurationSpringInitializer(p);
    oConf.setConfigId("testkey:testvalue,testkey2:testvalue2");
    boolean failed = false;
    try {
      oConf.test();
    } catch (Exception e) {
      Assert.assertEquals(e.getMessage().contains("can not find config for testkey:testvalue"), true);
      failed = true;
    }
    Assert.assertEquals(failed, true);
  }
}
