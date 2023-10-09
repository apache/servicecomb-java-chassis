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

package org.apache.servicecomb.provider.pojo.reference;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.provider.pojo.IPerson;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class PojoReferenceMetaTest {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void teardown() {
  }

  @Test
  public void testHasConsumerInterface() {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest(environment);

    PojoReferenceMeta pojoReferenceMeta = new PojoReferenceMeta();
    pojoReferenceMeta.setMicroserviceName("test");
    pojoReferenceMeta.setSchemaId("schemaId");
    pojoReferenceMeta.setConsumerIntf(IPerson.class);
    pojoReferenceMeta.afterPropertiesSet();

    Assertions.assertEquals(IPerson.class, pojoReferenceMeta.getObjectType());
    MatcherAssert.assertThat(pojoReferenceMeta.getProxy(), instanceOf(IPerson.class));
    Assertions.assertTrue(pojoReferenceMeta.isSingleton());

    scbEngine.destroy();
  }

  @Test
  public void testNoConsumerInterface() {
    PojoReferenceMeta pojoReferenceMeta = new PojoReferenceMeta();
    pojoReferenceMeta.setMicroserviceName("test");
    pojoReferenceMeta.setSchemaId("schemaId");

    try {
      pojoReferenceMeta.afterPropertiesSet();
      Assertions.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assertions.assertEquals(
          "microserviceName=test, schemaid=schemaId, \n"
              + "do not support implicit interface anymore, \n"
              + "because that caused problems:\n"
              + "  1.the startup process relies on other microservices\n"
              + "  2.cyclic dependent microservices can not be deployed\n"
              + "suggest to use @RpcReference or "
              + "<cse:rpc-reference id=\"...\" microservice-name=\"...\" schema-id=\"...\" interface=\"...\"></cse:rpc-reference>.",
          e.getMessage());
    }
  }
}
