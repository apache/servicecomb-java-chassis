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

package org.apache.servicecomb.provider.pojo.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestPojoSchemaMeta {

  PojoProducerMeta lPojoSchemaMeta = null;

  @Before
  public void setUp()
      throws Exception {
    lPojoSchemaMeta = new PojoProducerMeta();
  }

  @After
  public void tearDown()
      throws Exception {
    lPojoSchemaMeta = null;
  }

  @Test
  public void testGetImplementation(@Mocked PojoProducers producers)
      throws Exception {
    lPojoSchemaMeta.setImplementation("implementation");
    Deencapsulation.setField(lPojoSchemaMeta, "pojoProducers", producers);
    lPojoSchemaMeta.afterPropertiesSet();
    Assertions.assertEquals("implementation", lPojoSchemaMeta.getImplementation());
  }

  @Test
  public void testGetInstance()
      throws Exception {
    Object lObject = new Object();
    lPojoSchemaMeta.setInstance(lObject);
    Assertions.assertEquals(lObject, lPojoSchemaMeta.getInstance());
  }

  @Test
  public void testGetSchemaId()
      throws Exception {
    lPojoSchemaMeta.setSchemaId("schemaId");
    Assertions.assertEquals("schemaId", lPojoSchemaMeta.getSchemaId());
  }
}
