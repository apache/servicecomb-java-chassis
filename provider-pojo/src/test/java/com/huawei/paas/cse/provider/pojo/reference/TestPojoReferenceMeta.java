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

package com.huawei.paas.cse.provider.pojo.reference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.paas.cse.core.CseContext;
import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.schema.ConsumerSchemaFactory;
import com.huawei.paas.cse.core.provider.consumer.ConsumerProviderManager;
import com.huawei.paas.cse.core.provider.consumer.ReferenceConfig;
import com.huawei.paas.cse.provider.pojo.IPerson;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestPojoReferenceMeta {

    PojoReferenceMeta lPojoReferenceMeta = null;

    @Before
    public void setUp() throws Exception {
        lPojoReferenceMeta = new PojoReferenceMeta();
    }

    @After
    public void tearDown() throws Exception {
        lPojoReferenceMeta = null;
    }

    @Test
    public void testGetSchemaMeta() throws Exception {
        Assert.assertEquals(null, lPojoReferenceMeta.getSchemaMeta());
    }

    @Test
    public void testGetObject() throws Exception {
        lPojoReferenceMeta.setProxy(this);
        Assert.assertEquals(this, lPojoReferenceMeta.getObject());
    }

    @Test
    public void testGetObjectType() throws Exception {
        lPojoReferenceMeta.setConsumerIntf(this.getClass());
        Assert.assertEquals(this.getClass(), lPojoReferenceMeta.getObjectType());
    }

    @Test
    public void testGetProxy() throws Exception {
        lPojoReferenceMeta.setProxy(this);
        Assert.assertEquals(this, lPojoReferenceMeta.getProxy());
    }

    @Test
    public void testIsSingleton() throws Exception {
        Assert.assertEquals(true, lPojoReferenceMeta.isSingleton());
    }

    @Test
    public void test(@Mocked CseContext context, @Injectable ConsumerProviderManager manager,
            @Injectable ReferenceConfig config,
            @Injectable MicroserviceMeta microserviceMeta,
            @Injectable ConsumerSchemaFactory factory) {
        new Expectations() {
            {
                CseContext.getInstance().getConsumerProviderManager();
                result = manager;
                manager.getReferenceConfig("test");
                result = config;
                config.getMicroserviceMeta();
                result = microserviceMeta;
                microserviceMeta.ensureFindSchemaMeta("schemaId");
                CseContext.getInstance().getConsumerSchemaFactory();
                result = factory;

            }
        };
        PojoReferenceMeta lPojoReferenceMeta = new PojoReferenceMeta();
        lPojoReferenceMeta.setMicroserviceName("test");
        lPojoReferenceMeta.setSchemaId("schemaId");
        lPojoReferenceMeta.setConsumerIntf(IPerson.class);
        Assert.assertEquals(null, lPojoReferenceMeta.getReferenceConfig());
        Assert.assertEquals(IPerson.class, lPojoReferenceMeta.getConsumerIntf());
        lPojoReferenceMeta.createInvoker();
        lPojoReferenceMeta.afterPropertiesSet();
        Assert.assertEquals(config, lPojoReferenceMeta.getReferenceConfig());
    }
}
