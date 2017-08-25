/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.definition;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceMeta {
  MicroserviceMeta microservicemeta = new MicroserviceMeta("app:microservice");

  @Test
  public void testGetSchemaMetas() {
    Collection<SchemaMeta> schemaMetas = microservicemeta.getSchemaMetas();
    Assert.assertNotNull(schemaMetas);
  }

  @Test
  public void testGetExtData() {
    Object data = new Object();
    microservicemeta.putExtData("pruthi", data);
    Object response = microservicemeta.getExtData("pruthi");
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
      microservicemeta.ensureFindSchemaMeta(intf);
      Assert.assertEquals(1, 2);
    } catch (Throwable e) {
      Assert.assertEquals(
          "No schema interface is java.lang.Object.",
          e.getMessage());
    }
    microservicemeta.regSchemaMeta(sm1);
    Assert.assertEquals(sm1, microservicemeta.findSchemaMeta(intf));
    Assert.assertEquals(sm1, microservicemeta.ensureFindSchemaMeta(intf));

    microservicemeta.regSchemaMeta(sm2);
    Assert.assertEquals(sm1, microservicemeta.ensureFindSchemaMeta("a"));
    Assert.assertEquals(sm2, microservicemeta.ensureFindSchemaMeta("b"));
    try {
      microservicemeta.findSchemaMeta(intf);
      Assert.assertEquals(1, 2);
    } catch (Throwable e) {
      Assert.assertEquals(
          "More than one schema interface is java.lang.Object, please use schemaId to choose a schema.",
          e.getMessage());
    }
  }
}
