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

import javax.xml.ws.Holder;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.provider.producer.ProducerOperation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.servicecomb.core.Const;
import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.exception.CommonExceptionData;
import io.servicecomb.core.exception.InvocationException;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseSame;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.ReflectUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author
 * @version  [版本号, 2017年5月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestProducerSchemaFactory {
    private static ProducerSchemaFactory producerSchemaFactory = new ProducerSchemaFactory();

    private static SchemaMeta schemaMeta;

    public static class TestProducerSchemaFactoryImpl {
        public int add(int x, int y) {
            return x + y;
        }
    }

    @BeforeClass
    public static void init() {
        ProducerResponseMapperFactory responseMapperFactory = new ProducerResponseMapperFactory();
        responseMapperFactory.setMapperList(Arrays.asList(new ProducerResponseSame()));

        ProducerArgumentsMapperFactory producerArgsMapperFactory = new ProducerArgumentsMapperFactory();
        MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();
        SchemaLoader schemaLoader = new SchemaLoader() {
            @Override
            public void putSelfBasePathIfAbsent(String microserviceName, String basePath) {
            }
        };
        CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext = new CompositeSwaggerGeneratorContext();

        ReflectUtils.setField(producerSchemaFactory, "producerArgsMapperFactory", producerArgsMapperFactory);
        ReflectUtils.setField(producerSchemaFactory, "responseMapperFactory", responseMapperFactory);
        ReflectUtils.setField(producerSchemaFactory, "microserviceMetaManager", microserviceMetaManager);
        ReflectUtils.setField(producerSchemaFactory, "schemaLoader", schemaLoader);
        ReflectUtils.setField(producerSchemaFactory,
                "compositeSwaggerGeneratorContext",
                compositeSwaggerGeneratorContext);

        BeanUtils.setContext(Mockito.mock(ApplicationContext.class));

        UnitTestMeta.init();

        schemaMeta = producerSchemaFactory.getOrCreateProducerSchema("app:ms",
                "schema",
                TestProducerSchemaFactoryImpl.class,
                new TestProducerSchemaFactoryImpl());
    }

    @Test
    public void testGetOrCreateProducer() throws Exception {
        OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
        Assert.assertEquals("add", operationMeta.getOperationId());

        ProducerOperation producerOperation = operationMeta.getExtData(Const.PRODUCER_OPERATION);

        Object addBody = Class.forName("cse.gen.app.ms.schema.addBody").newInstance();
        ReflectUtils.setField(addBody, "x", 1);
        ReflectUtils.setField(addBody, "y", 2);
        Invocation invocation = new Invocation((Endpoint) null, operationMeta, new Object[] {addBody});
        Holder<Response> holder = new Holder<>();
        producerOperation.invoke(invocation, resp -> {
            holder.value = resp;
        });
        Assert.assertEquals(3, (int) holder.value.getResult());

        invocation = new Invocation((Endpoint) null, operationMeta, new Object[] {1, 2});
        producerOperation.invoke(invocation, resp -> {
            holder.value = resp;
        });
        Assert.assertEquals(true, holder.value.isFailed());
        InvocationException exception = (InvocationException) holder.value.getResult();
        CommonExceptionData data = (CommonExceptionData) exception.getErrorData();
        Assert.assertEquals("Cse Internal Server Error", data.getMessage());

    }
}
