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

import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.UnitTestRestUtils;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.junit.Assert;
import org.junit.Test;

public class TestMicroservicePaths {
  @Test
  public void testAddResourceStaticNewGroup() {
    RestOperationMeta staticRes = UnitTestRestUtils.createRestOperationMeta("POST", "/static");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(staticRes);

    Assert.assertSame(staticRes, paths.getStaticPathOperationMap().get("/static").findValue("POST"));
  }

  @Test
  public void testAddResourceStaticAddToGroup() {
    RestOperationMeta staticResPost = UnitTestRestUtils.createRestOperationMeta("POST", "/static");
    RestOperationMeta staticResGet = UnitTestRestUtils.createRestOperationMeta("GET", "/static");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(staticResPost);
    paths.addResource(staticResGet);

    Assert.assertSame(staticResPost, paths.getStaticPathOperationMap().get("/static").findValue("POST"));
    Assert.assertSame(staticResGet, paths.getStaticPathOperationMap().get("/static").findValue("GET"));
  }

  @Test
  public void testAddResourceStaticDuplicatedHttpMethod() {
    RestOperationMeta staticResPost = UnitTestRestUtils.createRestOperationMeta("POST", "/static");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(staticResPost);

    try {
      paths.addResource(staticResPost);
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals("operation with url /static, method POST is duplicated.", e.getMessage());
    }
  }

  @Test
  public void testAddResourceDynamic() {
    RestOperationMeta dynamicRes = UnitTestRestUtils.createRestOperationMeta("POST", "/dynamic/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(dynamicRes);

    Assert.assertSame(dynamicRes, paths.getDynamicPathOperationList().get(0));
  }

  @Test
  public void testCloneTo() {
    RestOperationMeta staticRes = UnitTestRestUtils.createRestOperationMeta("POST", "/static");
    RestOperationMeta dynamicRes = UnitTestRestUtils.createRestOperationMeta("POST", "/dynamic/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(staticRes);
    paths.addResource(dynamicRes);

    MicroservicePaths other = new MicroservicePaths();
    paths.cloneTo(other);

    Assert.assertEquals(paths.getStaticPathOperationMap(), other.getStaticPathOperationMap());
    Assert.assertEquals(paths.getDynamicPathOperationList(), other.getDynamicPathOperationList());
  }

  @Test
  public void testSortPath() {
    // only test base rule
    // completely rule test by TestRestOperationComparator
    RestOperationMeta dynamicResLessStatic = UnitTestRestUtils.createRestOperationMeta("POST", "/a/{id}");
    RestOperationMeta dynamicResMoreStatic = UnitTestRestUtils.createRestOperationMeta("POST", "/abc/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(dynamicResLessStatic);
    paths.addResource(dynamicResMoreStatic);
    paths.sortPath();

    Assert.assertSame(dynamicResMoreStatic, paths.getDynamicPathOperationList().get(0));
    Assert.assertSame(dynamicResLessStatic, paths.getDynamicPathOperationList().get(1));
  }

  @Test
  public void testPrintPaths() {
    RestOperationMeta staticRes = UnitTestRestUtils.createRestOperationMeta("POST", "/static");
    RestOperationMeta dynamicRes = UnitTestRestUtils.createRestOperationMeta("POST", "/dynamic/{id}");

    MicroservicePaths paths = new MicroservicePaths();
    paths.addResource(staticRes);
    paths.addResource(dynamicRes);

    WriterAppender appender = new WriterAppender();
    Writer writer = new StringWriter();
    appender.setWriter(writer);
    appender.setLayout(new SimpleLayout());
    Logger.getRootLogger().addAppender(appender);

    paths.printPaths();

    String[] lines = writer.toString().split("\n");
    Assert.assertEquals("INFO - Swagger mapped \"{[/static], method=[POST], produces=[application/json]}\" onto null",
        lines[0].trim());
    Assert.assertEquals(
        "INFO - Swagger mapped \"{[/dynamic/{id}], method=[POST], produces=[application/json]}\" onto null",
        lines[1].trim());

    Logger.getRootLogger().removeAppender(appender);
  }
}
