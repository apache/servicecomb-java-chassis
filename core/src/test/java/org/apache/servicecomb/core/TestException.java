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
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestException {
  @Test
  public void testCseException() {
    CseException oExeception = new CseException("500", "InternalServerError");
    Assertions.assertEquals("500", oExeception.getCode());
    Assertions.assertEquals("ServiceDefinitionException Code:500, Message:InternalServerError",
        oExeception.toString());

    oExeception = new CseException("503", "OwnException", new Throwable());
    Assertions.assertEquals("503", oExeception.getCode());
  }

  @Test
  public void testExceptionUtils() {
    CseException oExeception = ExceptionUtils
        .createCseException("servicecomb.handler.ref.not.exist", new String("test"));
    Assertions.assertEquals("servicecomb.handler.ref.not.exist", oExeception.getCode());

    oExeception =
        ExceptionUtils.createCseException("servicecomb.handler.ref.not.exist", new Throwable(), new String("test"));
    Assertions.assertEquals("servicecomb.handler.ref.not.exist", oExeception.getCode());

    oExeception = ExceptionUtils.producerOperationNotExist("servicecomb.error", "unit-testing");
    Assertions.assertEquals("servicecomb.producer.operation.not.exist", oExeception.getCode());

    oExeception = ExceptionUtils.handlerRefNotExist("servicecomb.double.error");
    Assertions.assertEquals("servicecomb.handler.ref.not.exist", oExeception.getCode());

    oExeception = ExceptionUtils.lbAddressNotFound("microServiceName", "my rule my world", "transportChannel");
    Assertions.assertEquals("servicecomb.lb.no.available.address", oExeception.getCode());
  }
}
