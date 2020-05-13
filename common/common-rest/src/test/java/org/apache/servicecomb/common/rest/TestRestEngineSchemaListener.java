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
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestRestEngineSchemaListener {
  static SCBEngine scbEngine;

  static ServicePathManager spm;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void setup() {
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();
    spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());
  }

  @AfterClass
  public static void teardown() {
    scbEngine.destroy();
  }

  @Test
  public void testLocateNotFound() {
    expectedException.expect(InvocationException.class);
    expectedException.expectMessage("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]");

    spm.producerLocateOperation("/notExist", "GET");
  }

  @Test
  public void testLocateNotFoundDynamicRemained() {
    expectedException.expect(InvocationException.class);
    expectedException.expectMessage("InvocationException: code=404;msg=CommonExceptionData [message=Not Found]");

    spm.producerLocateOperation("/dynamic/1/2", "GET");
  }

  @Test
  public void testLocateStaticMethodNotAllowed() {
    expectedException.expect(InvocationException.class);
    expectedException
        .expectMessage("InvocationException: code=405;msg=CommonExceptionData [message=Method Not Allowed]");

    spm.producerLocateOperation("/staticEx", "POST");
  }

  @Test
  public void testLocateDynamicMethodNotAllowed() {
    expectedException.expect(InvocationException.class);
    expectedException
        .expectMessage("InvocationException: code=405;msg=CommonExceptionData [message=Method Not Allowed]");

    spm.producerLocateOperation("/dynamic/1", "POST");
  }

  @Test
  public void testLocateStaticFound() {
    Assert.assertNotNull(spm.producerLocateOperation("/staticEx", "GET"));
  }

  @Test
  public void testLocateDynamicFound() {
    OperationLocator locator = spm.producerLocateOperation("/dynamic/1", "GET");
    Assert.assertEquals("1", locator.getPathVarMap().get("id"));
  }
}
