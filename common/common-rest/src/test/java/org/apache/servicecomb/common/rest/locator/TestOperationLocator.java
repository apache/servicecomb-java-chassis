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

package org.apache.servicecomb.common.rest.locator;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.UnitTestRestUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Assert;
import org.junit.Test;

public class TestOperationLocator {
  MicroservicePaths paths = new MicroservicePaths();

  OperationLocator locator = new OperationLocator();

  private RestOperationMeta addRestOperationMeta(String httpMethod, String path) {
    RestOperationMeta rom = UnitTestRestUtils.createRestOperationMeta(httpMethod, path);
    paths.addResource(rom);
    return rom;
  }

  @Test
  public void testLocateNotFound() {
    try {
      locator.locate("ms", "/notExist", "GET", paths);
      Assert.fail("must throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(Status.NOT_FOUND, e.getStatus());
    }
  }

  @Test
  public void testLocateNotFoundDynamicRemained() {
    addRestOperationMeta("GET", "/dynamic/{id}");
    try {
      locator.locate("ms", "/dynamic/1/2", "GET", paths);
      Assert.fail("must throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(Status.NOT_FOUND, e.getStatus());
    }
  }

  @Test
  public void testLocateStaticMethodNotAllowed() {
    addRestOperationMeta("GET", "/static");

    try {
      locator.locate("ms", "/static", "POST", paths);
      Assert.fail("must throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(Status.METHOD_NOT_ALLOWED, e.getStatus());
    }
  }

  @Test
  public void testLocateDynamicMethodNotAllowed() {
    addRestOperationMeta("GET", "/dynamic/{id}");
    try {
      locator.locate("ms", "/dynamic/1/", "POST", paths);
      Assert.fail("must throw exception");
    } catch (InvocationException e) {
      Assert.assertEquals(Status.METHOD_NOT_ALLOWED, e.getStatus());
    }
  }

  @Test
  public void testLocateStaticFound() {
    RestOperationMeta rom = addRestOperationMeta("GET", "/static");
    locator.locate("ms", "/static", "GET", paths);

    Assert.assertSame(rom, locator.getOperation());
  }

  @Test
  public void testLocateDynamicFound() {
    RestOperationMeta rom = addRestOperationMeta("GET", "/dynamic/{id}");
    locator.locate("ms", "/dynamic/1/", "GET", paths);

    Assert.assertSame(rom, locator.getOperation());
    Assert.assertEquals("1", locator.getPathVarMap().get("id"));
  }
}
