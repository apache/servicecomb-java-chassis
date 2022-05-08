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

package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.common.rest.locator.OperationLocator;
import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.common.rest.locator.TestPathSchema;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestRestEngineSchemaListener {
  static SCBEngine scbEngine;

  static ServicePathManager spm;

  @BeforeAll
  public static void setup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());
  }

  @AfterAll
  public static void teardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testLocateNotFound() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
            () -> spm.producerLocateOperation("/notExist", "GET"));
    Assertions.assertEquals("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]", exception.getMessage());
  }

  @Test
  public void testLocateNotFoundDynamicRemained() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
            () -> spm.producerLocateOperation("/dynamic/1/2", "GET"));
    Assertions.assertEquals("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]", exception.getMessage());
  }

  @Test
  public void testLocateStaticMethodNotAllowed() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
            () -> spm.producerLocateOperation("/staticEx", "POST"));
    Assertions.assertEquals("InvocationException: code=405;msg=CommonExceptionData [message=Method Not Allowed]", exception.getMessage());
  }

  @Test
  public void testLocateDynamicMethodNotAllowed() {
    InvocationException exception = Assertions.assertThrows(InvocationException.class,
            () -> spm.producerLocateOperation("/dynamic/1", "POST"));
    Assertions.assertEquals("InvocationException: code=405;msg=CommonExceptionData [message=Method Not Allowed]", exception.getMessage());
  }

  @Test
  public void testLocateStaticFound() {
    Assertions.assertNotNull(spm.producerLocateOperation("/staticEx", "GET"));
  }

  @Test
  public void testLocateDynamicFound() {
    OperationLocator locator = spm.producerLocateOperation("/dynamic/1", "GET");
    Assertions.assertEquals("1", locator.getPathVarMap().get("id"));
  }
}
