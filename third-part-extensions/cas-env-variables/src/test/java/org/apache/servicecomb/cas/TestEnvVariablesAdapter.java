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
package org.apache.servicecomb.cas;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestEnvVariablesAdapter {

  @BeforeClass
  public static void init() {
    System.setProperty("servicecomb.cas.application-id", "application-id");
    System.setProperty("servicecomb.cas.environment-id", "env-id");
  }

  @Test
  public void testProcessInstance() {
    CasEnvVariablesAdapter adapter = new CasEnvVariablesAdapter();
    MicroserviceInstance instance = new MicroserviceInstance();
    adapter.beforeRegisterInstance(instance);

    assertEquals(2, instance.getProperties().size());
    assertEquals("application-id", instance.getProperties().get("CAS_APPLICATION_ID"));
    assertEquals("env-id", instance.getProperties().get("CAS_ENVIRONMENT_ID"));
  }

  @AfterClass
  public static void destroy() {
    System.getProperties().remove("servicecomb.cas.application-id");
    System.getProperties().remove("servicecomb.cas.environment-id");
  }
}
