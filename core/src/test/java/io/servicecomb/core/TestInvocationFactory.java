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

import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;

import mockit.Injectable;

public class TestInvocationFactory {
    @BeforeClass
    public static void setUp() {
        System.out.println(
                DynamicPropertyFactory.getInstance().getStringProperty("service_description.name", null).get());
        Utils.updateProperty("service_description.name", "test");
        System.out.println(
                DynamicPropertyFactory.getInstance().getStringProperty("service_description.name", null).get());
    }

    @Test
    public void testInvocationFactoryforConsumer(@Injectable ReferenceConfig referenceConfig,
            @Injectable OperationMeta operationMeta) {
        Invocation invocation =
            InvocationFactory.forConsumer(referenceConfig, operationMeta, new String[] {"a", "b"});
        Assert.assertEquals(invocation.getContext(Const.SRC_MICROSERVICE), "test");
    }

    @Test
    public void testInvocationFactoryforConsumer(@Injectable ReferenceConfig referenceConfig,
            @Injectable SchemaMeta schemaMeta) {
        Invocation invocation =
            InvocationFactory.forConsumer(referenceConfig, schemaMeta, "test", new String[] {"a", "b"});
        Assert.assertEquals(invocation.getContext(Const.SRC_MICROSERVICE), "test");
    }

    @Test
    public void testInvocationFactoryforConsumer(@Injectable ReferenceConfig referenceConfig) {
        Invocation invocation =
            InvocationFactory.forConsumer(referenceConfig, "test", new String[] {"a", "b"});
        Assert.assertEquals(invocation.getContext(Const.SRC_MICROSERVICE), "test");
    }

    @Test
    public void testInvocationFactoryforProvider(@Injectable Endpoint endpoint,
            @Injectable OperationMeta operationMeta) {
        Invocation invocation =
            InvocationFactory.forProvider(endpoint, operationMeta, new String[] {"a", "b"});
        Assert.assertEquals(invocation.getEndpoint(), endpoint);
    }
}
