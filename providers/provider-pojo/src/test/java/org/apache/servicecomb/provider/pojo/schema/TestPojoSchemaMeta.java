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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestPojoSchemaMeta {

  PojoProducerMeta lPojoSchemaMeta = null;

  @BeforeEach
  public void setUp()
      throws Exception {
    lPojoSchemaMeta = new PojoProducerMeta();
  }

  @AfterEach
  public void tearDown()
      throws Exception {
    lPojoSchemaMeta = null;
  }

  @Test
  public void testGetImplementation() {
    PojoProducers producers = Mockito.mock(PojoProducers.class);
    lPojoSchemaMeta.setImplementation("implementation");
    lPojoSchemaMeta.setPojoProducers(producers);
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
