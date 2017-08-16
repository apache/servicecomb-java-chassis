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

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.provider.pojo.IPerson;
import io.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import mockit.Expectations;
import mockit.Injectable;

public class PojoReferenceMetaTest {
    @Test
    public void testHasConsumerInterface() {
        PojoReferenceMeta pojoReferenceMeta = new PojoReferenceMeta();
        pojoReferenceMeta.setMicroserviceName("test");
        pojoReferenceMeta.setSchemaId("schemaId");
        pojoReferenceMeta.setConsumerIntf(IPerson.class);

        pojoReferenceMeta.afterPropertiesSet();

        Assert.assertEquals(IPerson.class, pojoReferenceMeta.getObjectType());
        Object proxy = pojoReferenceMeta.getProxy();
        assertThat(proxy, instanceOf(IPerson.class));
        Assert.assertEquals(true, pojoReferenceMeta.isSingleton());

        // not recreate proxy
        pojoReferenceMeta.createInvoker();
        Assert.assertSame(proxy, pojoReferenceMeta.getProxy());
    }

    @Test
    public void testNoConsumerInterface() {
        PojoReferenceMeta pojoReferenceMeta = new PojoReferenceMeta();
        pojoReferenceMeta.setMicroserviceName("test");
        pojoReferenceMeta.setSchemaId("schemaId");

        pojoReferenceMeta.afterPropertiesSet();

        try {
            pojoReferenceMeta.getProxy();
            Assert.fail("must throw exception");
        } catch (ServiceCombException e) {
            Assert.assertEquals(
                    "Rpc reference  with service name [test] and schema [schemaId] is not populated",
                    e.getMessage());
        }
    }

    @Test
    public void testNoConsumerInterfaceCreateInvoke(@Injectable ConsumerProviderManager manager,
            @Injectable ReferenceConfig config,
            @Injectable MicroserviceMeta microserviceMeta,
            @Injectable ConsumerSchemaFactory factory,
            @Injectable SchemaMeta schemaMeta) {
        new Expectations() {
            {
                schemaMeta.getSwaggerIntf();
                result = IPerson.class;
            }
        };
        CseContext.getInstance().setConsumerProviderManager(manager);
        CseContext.getInstance().setConsumerSchemaFactory(factory);
        CseContext.getInstance().setSwaggerEnvironment(new BootstrapNormal().boot());

        PojoReferenceMeta pojoReferenceMeta = new PojoReferenceMeta();
        pojoReferenceMeta.setMicroserviceName("test");
        pojoReferenceMeta.setSchemaId("schemaId");
        pojoReferenceMeta.afterPropertiesSet();

        pojoReferenceMeta.createInvoker();
        assertThat(pojoReferenceMeta.getProxy(), instanceOf(IPerson.class));
    }
}
