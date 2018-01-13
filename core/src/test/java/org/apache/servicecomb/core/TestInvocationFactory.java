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

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Injectable;

public class TestInvocationFactory {
  @BeforeClass
  public static void setUp() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.setServiceRegistry(serviceRegistry);
  }

  @Test
  public void testInvocationFactoryforConsumer(@Injectable ReferenceConfig referenceConfig,
      @Injectable OperationMeta operationMeta) {
    Invocation invocation =
        InvocationFactory.forConsumer(referenceConfig, operationMeta, new String[] {"a", "b"});
    Assert.assertEquals("perfClient", invocation.getContext(Const.SRC_MICROSERVICE));
  }

  @Test
  public void testInvocationFactoryforConsumer(@Injectable ReferenceConfig referenceConfig,
      @Injectable SchemaMeta schemaMeta) {
    Invocation invocation =
        InvocationFactory.forConsumer(referenceConfig, schemaMeta, "test", new String[] {"a", "b"});
    Assert.assertEquals("perfClient", invocation.getContext(Const.SRC_MICROSERVICE));
  }

  @Test
  public void testInvocationFactoryforConsumer(@Injectable ReferenceConfig referenceConfig) {
    Invocation invocation =
        InvocationFactory.forConsumer(referenceConfig, "test", new String[] {"a", "b"});
    Assert.assertEquals("perfClient", invocation.getContext(Const.SRC_MICROSERVICE));
  }

  @Test
  public void testInvocationFactoryforProvider(@Injectable Endpoint endpoint,
      @Injectable OperationMeta operationMeta) {
    Invocation invocation =
        InvocationFactory.forProvider(endpoint, operationMeta, new String[] {"a", "b"});
    Assert.assertEquals(invocation.getEndpoint(), endpoint);
  }
}
