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
package org.apache.servicecomb.core.definition.schema;

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceVersionMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

public class TestConsumerSchemaFactory {
  class TestConsumerSchemaFactoryImpl {
    public int add(int x, int y) {
      return x + y;
    }
  }

  @Test
  public void createConsumerSchema() {
    UnitTestMeta meta = new UnitTestMeta();
    meta.registerSchema(new PojoSwaggerGeneratorContext(), TestConsumerSchemaFactoryImpl.class);

    MicroserviceVersionRule microserviceVersionRule = meta.getServiceRegistry().getAppManager()
        .getOrCreateMicroserviceVersionRule("app", "app:test", DefinitionConst.VERSION_RULE_ALL);
    MicroserviceMeta microserviceMeta = ((MicroserviceVersionMeta) microserviceVersionRule
        .getLatestMicroserviceVersion()).getMicroserviceMeta();

    OperationMeta operationMeta = microserviceMeta
        .ensureFindOperation(TestConsumerSchemaFactoryImpl.class.getName() + ".add");
    Assert.assertEquals("add", operationMeta.getOperationId());
  }
}
