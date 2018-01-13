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

import org.apache.servicecomb.core.exception.CseException;
import org.apache.servicecomb.core.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestException {
  @Test
  public void testCseException() {
    CseException oExeception = new CseException("500", "InternalServerError");
    Assert.assertEquals("500", oExeception.getCode());
    Assert.assertEquals("ServiceDefinitionException Code:500, Message:InternalServerError",
        oExeception.toString());

    oExeception = new CseException("503", "OwnException", new Throwable());
    Assert.assertEquals("503", oExeception.getCode());
  }

  @Test
  public void testExceptionUtils() {
    CseException oExeception = ExceptionUtils.createCseException("cse.handler.ref.not.exist", new String("test"));
    Assert.assertEquals("cse.handler.ref.not.exist", oExeception.getCode());

    oExeception =
        ExceptionUtils.createCseException("cse.handler.ref.not.exist", new Throwable(), new String("test"));
    Assert.assertEquals("cse.handler.ref.not.exist", oExeception.getCode());

    oExeception = ExceptionUtils.producerOperationNotExist("cse.error", "unit-testing");
    Assert.assertEquals("cse.producer.operation.not.exist", oExeception.getCode());

    oExeception = ExceptionUtils.operationIdInvalid("cse.double.error", "what path are you talking about");
    Assert.assertEquals("cse.schema.operation.id.invalid", oExeception.getCode());

    oExeception = ExceptionUtils.handlerRefNotExist("cse.double.error");
    Assert.assertEquals("cse.handler.ref.not.exist", oExeception.getCode());

    oExeception = ExceptionUtils.lbAddressNotFound("microServiceName", "my rule my world", "transportChannel");
    Assert.assertEquals("cse.lb.no.available.address", oExeception.getCode());
  }
}
