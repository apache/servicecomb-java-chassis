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

package io.servicecomb.provider.pojo.reference;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.provider.pojo.IPerson;
import io.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import mockit.Expectations;
import mockit.Injectable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PojoReferenceMetaTest {

    private PojoReferenceMeta pojoReferenceMeta = new PojoReferenceMeta();

    @Before
    public void setUp() throws Exception {
        pojoReferenceMeta.setMicroserviceName("test");
        pojoReferenceMeta.setSchemaId("schemaId");
        pojoReferenceMeta.setConsumerIntf(IPerson.class);
    }

    @Test
    public void testGetSchemaMeta() throws Exception {
        Assert.assertEquals(null, pojoReferenceMeta.getSchemaMeta());
    }

    @Test
    public void testGetObjectType() throws Exception {
        Assert.assertEquals(IPerson.class, pojoReferenceMeta.getObjectType());
    }

    @Test
    public void testGetProxy() throws Exception {
        pojoReferenceMeta.createProxy();
        assertThat(pojoReferenceMeta.getProxy(), instanceOf(IPerson.class));
    }

    @Test
    public void testIsSingleton() throws Exception {
        Assert.assertEquals(true, pojoReferenceMeta.isSingleton());
    }

    @Test
    public void test(@Injectable ConsumerProviderManager manager,
            @Injectable ReferenceConfig config,
            @Injectable MicroserviceMeta microserviceMeta,
            @Injectable ConsumerSchemaFactory factory) {
        new Expectations() {
            {
                manager.getReferenceConfig("test");
                result = config;
                config.getMicroserviceMeta();
                result = microserviceMeta;
                microserviceMeta.ensureFindSchemaMeta("schemaId");
            }
        };
        CseContext.getInstance().setConsumerProviderManager(manager);
        CseContext.getInstance().setConsumerSchemaFactory(factory);
        CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());

        Assert.assertEquals(null, pojoReferenceMeta.getReferenceConfig());
        Assert.assertEquals(IPerson.class, pojoReferenceMeta.getConsumerIntf());
        pojoReferenceMeta.createInvoker();
        pojoReferenceMeta.afterPropertiesSet();
        Assert.assertEquals(config, pojoReferenceMeta.getReferenceConfig());
    }
}
