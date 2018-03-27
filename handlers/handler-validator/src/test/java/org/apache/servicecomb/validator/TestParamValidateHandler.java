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
package org.apache.servicecomb.validator;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestParamValidateHandler {

  ParamValidateHandler handler;

  Invocation invocation;

  AsyncResponse asyncResp;

  @Before
  public void setUP() {
    handler = new ParamValidateHandler();
    invocation = Mockito.mock(Invocation.class);
    asyncResp = Mockito.mock(AsyncResponse.class);
  }

  @After
  public void afterTest() {
    handler = null;
    invocation = null;
    asyncResp = null;
  }

  @Test
  public void testHandleForException() throws Exception {
    Person obj = new Person();
    obj.setName("test");
    obj.setAge(11);
    Mockito.when(invocation.getArgs()).thenReturn(new Person[] {obj});
    handler.handle(invocation, ar -> {
      InvocationException exp = ar.getResult();
      Assert.assertNotNull(exp.getErrorData());
    });
  }

  @Test
  public void testHandleException() throws Exception {
    Person obj = new Person();
    obj.setName("");
    obj.setAge(11);
    Mockito.when(invocation.getArgs()).thenReturn(new Person[] {obj});
    handler.handle(invocation, ar -> {
      InvocationException exp = ar.getResult();
      Assert.assertNotNull(exp.getErrorData());
    });
  }

  @Test
  public void testHandleInvalidClass() throws Exception {
    Person obj = new Person();
    obj.setName("");
    obj.setAge(11);
    obj.setAddr("");
    Mockito.when(invocation.getArgs()).thenReturn(new Person[] {obj});
    handler.handle(invocation, ar -> {
      InvocationException exp = ar.getResult();
      Assert.assertNotNull(exp.getErrorData());
    });
  }

  @Test
  public void testHandle() throws Exception {
    boolean isSuccess = true;
    Person obj = new Person();
    obj.setName("test");
    obj.setAge(21);
    obj.setAddr("addr");
    try {
      Mockito.when(invocation.getArgs()).thenReturn(new Person[] {obj});
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      isSuccess = false;
    }
    Assert.assertTrue(isSuccess);
  }
}
