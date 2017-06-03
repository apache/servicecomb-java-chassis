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
package io.servicecomb.core.definition.loader;

import java.util.Collections;

import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.unittest.UnitTestMeta;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.foundation.common.utils.ReflectUtils;

public class TestDynamicSchemaLoader {
    private static MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

    @BeforeClass
    public static void init() {
        UnitTestMeta.init();

        SchemaLoader loader = new SchemaLoader();
        loader.setMicroserviceMetaManager(microserviceMetaManager);

        SchemaListenerManager schemaListenerManager = new SchemaListenerManager();
        schemaListenerManager.setSchemaListenerList(Collections.emptyList());

        CseContext context = CseContext.getInstance();
        context.setSchemaLoader(loader);
        context.setSchemaListenerManager(schemaListenerManager);

        Microservice microservice = new Microservice();
        microservice.setAppId("app");
        microservice.setServiceName("ms");
        ReflectUtils.setField(RegistryUtils.class, null, "microservice", microservice);
    }

    @Test
    public void testRegisterSchemas() {
        DynamicSchemaLoader.INSTANCE.registerSchemas("classpath*:test/test/schema.yaml");
        SchemaMeta schemaMeta = microserviceMetaManager.ensureFindSchemaMeta("ms", "schema");
        Assert.assertEquals("cse.gen.app.ms.schema", schemaMeta.getPackageName());
    }
}
