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

package org.apache.servicecomb.swagger.generator.core;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGenerator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSwaggerGenerator {
  @BeforeClass
  public static void setup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testBasePathPlaceHolder() {
    ArchaiusUtils.setProperty("var", "varValue");

    PojoSwaggerGenerator swaggerGenerator = new PojoSwaggerGenerator(null);
    swaggerGenerator.setBasePath("/a/${var}/b");

    Assert.assertEquals("/a/varValue/b", swaggerGenerator.getSwagger().getBasePath());
  }

  @Test
  public void testAddDefaultTag() {
    PojoSwaggerGenerator swaggerGenerator = new PojoSwaggerGenerator(null);

    swaggerGenerator.addDefaultTag("test1");
    swaggerGenerator.addDefaultTag("");
    swaggerGenerator.addDefaultTag(null);
    swaggerGenerator.addDefaultTag("test2");

    assertThat(swaggerGenerator.getDefaultTags(), contains("test1", "test2"));
  }
}
