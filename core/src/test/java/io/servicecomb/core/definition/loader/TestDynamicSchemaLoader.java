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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;

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

        ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
        serviceRegistry.init();

        RegistryUtils.setServiceRegistry(serviceRegistry);
    }

    @AfterClass
    public static void teardown() {
        RegistryUtils.setServiceRegistry(null);
    }

    @Test
    public void testRegisterSchemas() {
        DynamicSchemaLoader.INSTANCE.registerSchemas("classpath*:test/test/schema.yaml");
        SchemaMeta schemaMeta = microserviceMetaManager.ensureFindSchemaMeta("perfClient", "schema");
        Assert.assertEquals("cse.gen.pojotest.perfClient.schema", schemaMeta.getPackageName());
    }
}
