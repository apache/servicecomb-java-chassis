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
package org.apache.servicecomb.serviceregistry.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Test;

public class TestEnvAdapterManager {

  private EnvAdapterManager manager = EnvAdapterManager.INSTANCE;

  @Test
  public void testLoadAdapter() {
    assertEquals(3, manager.values().size());
    assertNull(manager.findValue("cas-env-three"));
    assertNotNull(manager.findValue("default-env-adapter"));
    assertNotNull(manager.findValue("cas_env_one"));
    assertNotNull(manager.findValue("cas_env_two"));

    assertEquals(0, manager.findValue("cas_env_one").getOrder());
    assertEquals(0, manager.findValue("cas_env_two").getOrder());
    assertEquals(0, manager.findValue("default-env-adapter").getOrder());
  }

  @Test
  public void testProcessMicroservice() {
    Microservice microservice = new Microservice();
    manager.processMicroserviceWithAdapters(microservice);

    assertEquals("order=0", microservice.getProperties().get("cas_env_one"));
    assertEquals("order=0", microservice.getProperties().get("cas_env_two"));
    assertNull(microservice.getProperties().get("default-env-adapter"));
  }

  @Test
  public void testProcessInstance() {
    MicroserviceInstance instance = new MicroserviceInstance();
    manager.processInstanceWithAdapters(instance);

    assertEquals("order=0", instance.getProperties().get("cas_env_one"));
    assertEquals("order=0", instance.getProperties().get("cas_env_two"));
    assertNull(instance.getProperties().get("default-env-adapter"));
  }
}
