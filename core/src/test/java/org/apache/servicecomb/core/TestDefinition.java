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

import org.apache.servicecomb.core.definition.CommonService;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Info;
import io.swagger.models.Swagger;

public class TestDefinition {
  @Test
  public void testCommonService() {
    CommonService<String> oCommonService = new CommonService<>();

    boolean validCreateOperation = true;
    try {
      oCommonService.createOperationMgr("test1");
    } catch (Exception e) {
      validCreateOperation = false;
    }
    Assert.assertTrue(validCreateOperation);
    oCommonService.setName("test1");
    boolean validRegOperation = true;
    try {
      oCommonService.regOperation("oName1", "op1");
    } catch (Exception e) {
      validRegOperation = false;
    }
    Assert.assertTrue(validRegOperation);

    Assert.assertEquals(1, oCommonService.getOperations().size());
    Assert.assertEquals("test1", oCommonService.getName());
    Assert.assertEquals("op1", oCommonService.findOperation("oName1"));
    Assert.assertEquals("op1", oCommonService.ensureFindOperation("oName1"));
  }

  @Test
  public void testMicroServiceMeta() {
    MicroserviceMeta oMicroMeta = new MicroserviceMeta("app:micro1");
    Assert.assertEquals(0, oMicroMeta.getSchemaMetas().size());
    Assert.assertEquals(0, oMicroMeta.getOperations().size());
    Assert.assertEquals("micro1", oMicroMeta.getShortName());
    Assert.assertEquals("app:micro1", oMicroMeta.getName());
    try {
      oMicroMeta.putExtData("key1", new String("value1"));
      Assert.assertNotEquals(null, oMicroMeta.getExtData("key1"));
    } catch (Exception e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testMicroserviceMetaManager() throws Exception {
    MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();
    microserviceMetaManager.getOrCreateMicroserviceMeta("app:testname");

    Assert.assertEquals("microservice meta manager", microserviceMetaManager.getName());
    Assert.assertEquals("Not allow register repeat data, name=%s, key=%s",
        microserviceMetaManager.getRegisterErrorFmt());
    Assert.assertEquals(0, microserviceMetaManager.getAllSchemaMeta("app:testname").size());

    Swagger oSwagger = new Swagger();
    Info oInfo = new Info();
    oInfo.setVendorExtension("x-java-interface", "java.lang.String");
    oSwagger.setInfo(oInfo);
    Assert.assertEquals("java.lang.String", (ClassUtils.getJavaInterface(oSwagger)).getName());
    oInfo.setVendorExtension("x-java-class", "java.lang.String");
  }
}
