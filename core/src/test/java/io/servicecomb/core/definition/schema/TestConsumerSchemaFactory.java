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
package io.servicecomb.core.definition.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.servicecomb.core.provider.consumer.ConsumerOperationMeta;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaListener;
import io.servicecomb.core.definition.loader.SchemaListenerManager;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.RegistryClientFactory;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentSame;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentToBodyField;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import io.servicecomb.swagger.invocation.response.consumer.ConsumerResponseSame;
import io.servicecomb.foundation.common.utils.ReflectUtils;

public class TestConsumerSchemaFactory {
    private static ConsumerSchemaFactory consumerSchemaFactory = new ConsumerSchemaFactory();

    private static ServiceRegistryClient registryClient = Mockito.mock(ServiceRegistryClient.class);

    private static SchemaListener schemaListener = new SchemaListener() {

        @Override
        public void onSchemaLoaded(SchemaMeta... schemaMetas) {

        }

    };

    static interface Intf {
        int add(int x, int y);
    }

    class TestConsumerSchemaFactoryImpl {
        public int add(int x, int y) {
            return x + y;
        }
    }

    @BeforeClass
    public static void init() {
        ReflectUtils.setField(RegistryClientFactory.class, null, "registryClient", registryClient);

        SchemaListenerManager schemaListenerManager = new SchemaListenerManager();
        schemaListenerManager.setSchemaListenerList(Arrays.asList(schemaListener));

        ConsumerResponseMapperFactory responseMapperFactory = new ConsumerResponseMapperFactory();
        responseMapperFactory.setMapperList(Arrays.asList(new ConsumerResponseSame()));

        ConsumerArgumentsMapperFactory consumerArgsMapperFactory = new ConsumerArgumentsMapperFactory();
        MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();
        SchemaLoader schemaLoader = new SchemaLoader();
        CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext = new CompositeSwaggerGeneratorContext();

        ReflectUtils.setField(consumerSchemaFactory, "schemaListenerManager", schemaListenerManager);
        ReflectUtils.setField(consumerSchemaFactory, "consumerArgsMapperFactory", consumerArgsMapperFactory);
        ReflectUtils.setField(consumerSchemaFactory, "responseMapperFactory", responseMapperFactory);
        ReflectUtils.setField(consumerSchemaFactory, "microserviceMetaManager", microserviceMetaManager);
        ReflectUtils.setField(consumerSchemaFactory, "schemaLoader", schemaLoader);
        ReflectUtils.setField(consumerSchemaFactory,
                "compositeSwaggerGeneratorContext",
                compositeSwaggerGeneratorContext);

        SchemaMeta schemaMeta = new UnitTestMeta().getOrCreateSchemaMeta(TestConsumerSchemaFactoryImpl.class);
        String content = UnitTestSwaggerUtils.pretty(schemaMeta.getSwagger());

        Mockito.when(registryClient.getMicroserviceId("app", "ms", "latest")).thenReturn("0");
        Mockito.when(registryClient.getSchema("0", "schema")).thenReturn(content);

        Microservice microservice = new Microservice();
        microservice.setAppId("app");
        microservice.setServiceId("0");
        microservice.addSchema("schema", content);
        Mockito.when(registryClient.getMicroservice("0")).thenReturn(microservice);
    }

    @Test
    public void testGetOrCreateConsumer() {
        MicroserviceMeta microserviceMeta =
            consumerSchemaFactory.getOrCreateConsumer("ms", "latest");
        OperationMeta operationMeta = microserviceMeta.ensureFindOperation("schema.add");
        Assert.assertEquals("add", operationMeta.getOperationId());
    }

    @Test
    public void testConnectToConsumerSame() {
        MicroserviceMeta microserviceMeta =
            consumerSchemaFactory.getOrCreateConsumer("ms", "latest");
        SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta("schema");

        Map<String, ConsumerOperationMeta> consumerOperationMap = new HashMap<>();
        consumerSchemaFactory.connectToConsumer(schemaMeta, null, consumerOperationMap);
        Assert.assertEquals(1, consumerOperationMap.size());

        ConsumerOperationMeta consumerOperationMeta = consumerOperationMap.get("add");
        Assert.assertEquals(ConsumerArgumentSame.class,
                consumerOperationMeta.getArgsMapper().getArgumentMapper(0).getClass());
        Assert.assertEquals(ConsumerResponseSame.class,
                consumerOperationMeta.getResponseMapper().getClass());
    }

    @Test
    public void testConnectToConsumerDiff() {
        MicroserviceMeta microserviceMeta =
            consumerSchemaFactory.getOrCreateConsumer("ms", "latest");
        SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta("schema");

        Map<String, ConsumerOperationMeta> consumerOperationMap = new HashMap<>();
        consumerSchemaFactory.connectToConsumer(schemaMeta, Intf.class, consumerOperationMap);
        Assert.assertEquals(1, consumerOperationMap.size());

        ConsumerOperationMeta consumerOperationMeta = consumerOperationMap.get("add");
        Assert.assertEquals(ConsumerArgumentToBodyField.class,
                consumerOperationMeta.getArgsMapper().getArgumentMapper(0).getClass());
        Assert.assertEquals(ConsumerResponseSame.class,
                consumerOperationMeta.getResponseMapper().getClass());
    }

}
