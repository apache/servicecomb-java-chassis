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

import java.lang.reflect.InvocationTargetException;

import jakarta.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestException {
  @Test
  public void testCommonExceptionData() {
    CommonExceptionData oData = new CommonExceptionData();
    oData.setMessage("this is Common exception message");
    Assertions.assertEquals("this is Common exception message", oData.getMessage());

    oData = new CommonExceptionData("this is a test");
    Assertions.assertEquals("this is a test", oData.getMessage());
    Assertions.assertEquals("CommonExceptionData [message=this is a test]", oData.toString());
  }

  @Test
  public void testInvocationException() {
    InvocationException oExceptionIn = new InvocationException(Status.OK, "I am gone now");
    oExceptionIn = ExceptionFactory.convertConsumerException(new Throwable());
    Assertions.assertEquals(490, oExceptionIn.getStatusCode());

    oExceptionIn = ExceptionFactory.convertConsumerException(new Throwable(), "abc");
    Assertions.assertEquals(490, oExceptionIn.getStatusCode());
    Assertions.assertEquals("abc", ((CommonExceptionData) oExceptionIn.getErrorData()).getMessage());

    oExceptionIn = ExceptionFactory.convertProducerException(new Throwable());
    Assertions.assertEquals(590, oExceptionIn.getStatusCode());

    oExceptionIn = ExceptionFactory.convertProducerException(new Throwable(), "abcd");
    Assertions.assertEquals(590, oExceptionIn.getStatusCode());
    Assertions.assertEquals("abcd", ((CommonExceptionData) oExceptionIn.getErrorData()).getMessage());

    oExceptionIn =
        ExceptionFactory.convertConsumerException(new InvocationException(Status.OK, new String("fake-object")));
    Assertions.assertEquals(200, oExceptionIn.getStatusCode());

    oExceptionIn = ExceptionFactory.convertConsumerException(new InvocationTargetException(new Throwable()));
    Assertions.assertNotEquals("java.lang.Throwable", oExceptionIn.getMessage());

    InvocationException oTemp = new InvocationException(Status.OK, new CommonExceptionData("testObject"));
    Assertions.assertEquals("OK", oTemp.getReasonPhrase());
    Assertions.assertEquals("CommonExceptionData [message=testObject]", (oTemp.getErrorData().toString()));
  }
}
