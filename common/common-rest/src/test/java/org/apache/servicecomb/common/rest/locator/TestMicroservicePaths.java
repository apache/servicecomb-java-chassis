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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestMicroservicePaths {
  static SCBEngine scbEngine;

  static MicroservicePaths paths;

  @BeforeAll
  public static void setup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new TestPathSchema())
        .run();

    ServicePathManager spm = ServicePathManager.getServicePathManager(scbEngine.getProducerMicroserviceMeta());
    paths = spm.producerPaths;
  }

  @AfterAll
  public static void teardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void staticGroup() {
    RestOperationMeta meta = paths.getStaticPathOperationMap().get("/static/").findValue("POST");
    Assertions.assertSame("postStatic", meta.getOperationMeta().getOperationId());

    meta = paths.getStaticPathOperationMap().get("/static/").findValue("GET");
    Assertions.assertSame("getStatic", meta.getOperationMeta().getOperationId());
  }

  @Test
  public void testAddResourceStaticDuplicatedHttpMethod() {
    RestOperationMeta staticResPost = Mockito.mock(RestOperationMeta.class);
    Mockito.when(staticResPost.getHttpMethod()).thenReturn("POST");
    Mockito.when(staticResPost.getAbsolutePath()).thenReturn("/static/");
    Mockito.when(staticResPost.isAbsoluteStaticPath()).thenReturn(true);


    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> paths.addResource(staticResPost));
    Assertions.assertEquals("operation with url /static/, method POST is duplicated.", exception.getMessage());
  }

  @Test
  public void dynamicPath() {
    Assertions.assertEquals("dynamicExId", paths.getDynamicPathOperationList().get(0).getOperationMeta().getOperationId());
    Assertions.assertEquals("dynamicId", paths.getDynamicPathOperationList().get(1).getOperationMeta().getOperationId());
  }

  @Test
  public void testPrintPaths() {
    try (LogCollector collector = new LogCollector()) {
      paths.printPaths();

      StringBuilder sb = new StringBuilder();
      collector.getEvents().stream()
          .forEach(e -> sb.append(e.getMessage()).append("\n"));
      Assertions.assertEquals(
          "Swagger mapped \"{[/static/], method=[POST], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.postStatic()\n"
              + "Swagger mapped \"{[/static/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.getStatic()\n"
              + "Swagger mapped \"{[/staticEx/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.getStaticEx()\n"
              + "Swagger mapped \"{[/dynamicEx/{id}/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.dynamicExId(java.lang.String)\n"
              + "Swagger mapped \"{[/dynamic/{id}/], method=[GET], produces=[application/json]}\" onto public void org.apache.servicecomb.common.rest.locator.TestPathSchema.dynamicId(java.lang.String)\n",
          sb.toString());
    }
  }
}
