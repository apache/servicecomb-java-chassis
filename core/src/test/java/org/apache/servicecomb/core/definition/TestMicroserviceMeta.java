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

package org.apache.servicecomb.core.definition;

import java.util.Collection;

import org.apache.servicecomb.core.definition.classloader.MicroserviceClassLoader;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceMeta {
  MicroserviceMeta microserviceMeta = new MicroserviceMeta("app:microservice");

  @Test
  public void classloader() {
    ClassLoader loader = new MicroserviceClassLoader("", "", "");
    microserviceMeta.setClassLoader(loader);
    Assert.assertSame(loader, microserviceMeta.getClassLoader());
  }

  @Test
  public void testGetSchemaMetas() {
    Collection<SchemaMeta> schemaMetas = microserviceMeta.getSchemaMetas();
    Assert.assertNotNull(schemaMetas);
  }

  @Test
  public void testGetExtData() {
    Object data = new Object();
    microserviceMeta.putExtData("pruthi", data);
    Object response = microserviceMeta.getExtData("pruthi");
    Assert.assertNotNull(response);
  }

  @Test
  public void testIntf(@Mocked SchemaMeta sm1, @Mocked SchemaMeta sm2) {
    Class<?> intf = Object.class;
    new Expectations() {
      {
        sm1.getSchemaId();
        result = "a";
        sm2.getSchemaId();
        result = "b";
        sm1.getSwaggerIntf();
        result = intf;
        sm2.getSwaggerIntf();
        result = intf;
      }
    };

    try {
      microserviceMeta.ensureFindSchemaMeta(intf);
      Assert.assertEquals(1, 2);
    } catch (Throwable e) {
      Assert.assertEquals(
          "No schema interface is java.lang.Object.",
          e.getMessage());
    }
    microserviceMeta.regSchemaMeta(sm1);
    Assert.assertEquals(sm1, microserviceMeta.findSchemaMeta(intf));
    Assert.assertEquals(sm1, microserviceMeta.ensureFindSchemaMeta(intf));

    microserviceMeta.regSchemaMeta(sm2);
    Assert.assertEquals(sm1, microserviceMeta.ensureFindSchemaMeta("a"));
    Assert.assertEquals(sm2, microserviceMeta.ensureFindSchemaMeta("b"));
    try {
      microserviceMeta.findSchemaMeta(intf);
      Assert.assertEquals(1, 2);
    } catch (Throwable e) {
      Assert.assertEquals(
          "More than one schema interface is java.lang.Object, please use schemaId to choose a schema.",
          e.getMessage());
    }
  }
}
