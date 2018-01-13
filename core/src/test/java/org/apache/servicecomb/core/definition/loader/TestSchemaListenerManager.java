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

package org.apache.servicecomb.core.definition.loader;

import java.util.Arrays;

import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestSchemaListenerManager {

  SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);

  @Before
  public void setUp() {
    Mockito.when(schemaMeta.getSchemaId()).thenReturn("test");
  }

  @Test
  public void testInitializationListener() {
    SchemaListener listener = new SchemaListener() {
      @Override
      public void onSchemaLoaded(SchemaMeta... schemaMetas) {
        Assert.assertEquals(1, schemaMetas.length);
        Assert.assertEquals("test", schemaMetas[0].getSchemaId());
      }
    };

    SchemaListenerManager mgr = new SchemaListenerManager();
    mgr.setSchemaListenerList(Arrays.asList(listener));
    mgr.setMicroserviceMetaManager(new MicroserviceMetaManager());

    mgr.notifySchemaListener(schemaMeta);
  }
}
