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

import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestMicroservicePaths {
  static SCBEngine scbEngine;

  static MicroservicePaths paths;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void setup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();

    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());
    paths = Deencapsulation.getField(spm, "producerPaths");
  }

  @AfterClass
  public static void teardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void staticGroup() {
    RestOperationMeta meta = paths.getStaticPathOperationMap().get("/static/").findValue("POST");
    Assert.assertSame("postStatic", meta.getOperationMeta().getOperationId());

    meta = paths.getStaticPathOperationMap().get("/static/").findValue("GET");
    Assert.assertSame("getStatic", meta.getOperationMeta().getOperationId());
  }

  @Test
  public void testAddResourceStaticDuplicatedHttpMethod(@Mocked RestOperationMeta staticResPost) {
    new Expectations() {
      {
        staticResPost.getHttpMethod();
        result = "POST";
        staticResPost.getAbsolutePath();
        result = "/static/";
        staticResPost.isAbsoluteStaticPath();
        result = true;
      }
    };

    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage("operation with url /static/, method POST is duplicated.");

    paths.addResource(staticResPost);
  }

  @Test
  public void dynamicPath() {
    Assert.assertEquals("dynamicExId", paths.getDynamicPathOperationList().get(0).getOperationMeta().getOperationId());
    Assert.assertEquals("dynamicId", paths.getDynamicPathOperationList().get(1).getOperationMeta().getOperationId());
  }

  @Test
  public void testPrintPaths() {
    try (LogCollector collector = new LogCollector()) {
      paths.printPaths();

      StringBuilder sb = new StringBuilder();
      collector.getEvents().stream()
          .forEach(e -> sb.append(e.getMessage()).append("\n"));
      Assert.assertEquals(
          "Swagger mapped \"{[/static/], method=[POST], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.postStatic()\n"
              + "Swagger mapped \"{[/static/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.getStatic()\n"
              + "Swagger mapped \"{[/staticEx/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.getStaticEx()\n"
              + "Swagger mapped \"{[/dynamicEx/{id}/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.dynamicExId(java.lang.String)\n"
              + "Swagger mapped \"{[/dynamic/{id}/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.dynamicId(java.lang.String)\n",
          sb.toString());
    }
  }
}
