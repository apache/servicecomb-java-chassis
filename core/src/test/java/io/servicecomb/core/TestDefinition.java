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

package io.servicecomb.core;

import io.servicecomb.core.definition.CommonService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;

import io.swagger.models.Info;
import io.swagger.models.Swagger;
import mockit.Mock;
import mockit.MockUp;

public class TestDefinition {

    CommonService<String> oCommonService = null;

    @BeforeClass
    public static void setupClass() throws Exception {
        Microservice microservice = new Microservice();
        microservice.setAppId("app");
        microservice.setServiceName("testname");

        new MockUp<RegistryUtils>() {
            @Mock
            private Microservice createMicroserviceFromDefinition() {
                return microservice;
            }
        };
    }

    @Before
    public void setup() throws Exception {
        oCommonService = new CommonService<>();
    }

    @After
    public void tearDown() throws Exception {
        oCommonService = null;
    }

    /**
     * TestCommonService
     */
    @Test
    public void testCommonService() {

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

    /**
     * Test MicroService
     * @throws Exception
     */
    @Test
    public void testMicroServiceMeta() {
        MicroserviceMeta oMicroMeta = new MicroserviceMeta("micro1");
        Assert.assertEquals(0, oMicroMeta.getSchemaMetas().size());
        Assert.assertEquals(0, oMicroMeta.getOperations().size());
        Assert.assertEquals("micro1", oMicroMeta.getName());
        try {
            oMicroMeta.putExtData("key1", new String("value1"));
            Assert.assertNotEquals(null, oMicroMeta.getExtData("key1"));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test MicroserviceMetaManager
     * @throws Exception 
     */
    @Test
    public void testMicroserviceMetaManager() throws Exception {
        MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();
        microserviceMetaManager.getOrCreateMicroserviceMeta("testname");

        Assert.assertEquals("microservice meta manager", microserviceMetaManager.getName());
        Assert.assertEquals("Not allow regsiter repeat data, name=%s, key=%s",
                microserviceMetaManager.getRegisterErrorFmt());
        Assert.assertEquals(0, microserviceMetaManager.getAllSchemaMeta("testname").size());

        Swagger oSwagger = new Swagger();
        Info oInfo = new Info();
        oInfo.setVendorExtension("x-java-interface", "java.lang.String");
        oSwagger.setInfo(oInfo);
        Assert.assertEquals("java.lang.String", (ClassUtils.getJavaInterface(oSwagger)).getName());
        oInfo.setVendorExtension("x-java-class", "java.lang.String");
    }
}
