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

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestEnvAdapterManager {

  private final EnvAdapterManager manager = EnvAdapterManager.INSTANCE;

  @Test
  public void testLoadAdapter() {
    Assertions.assertEquals(3, manager.values().size());
    Assertions.assertNull(manager.findValue("cas-env-three"));
    Assertions.assertNotNull(manager.findValue("default-env-adapter"));
    Assertions.assertNotNull(manager.findValue("cas_env_one"));
    Assertions.assertNotNull(manager.findValue("cas_env_two"));

    Assertions.assertEquals(0, manager.findValue("cas_env_one").getOrder());
    Assertions.assertEquals(0, manager.findValue("cas_env_two").getOrder());
    Assertions.assertEquals(0, manager.findValue("default-env-adapter").getOrder());
  }

  @Test
  public void testProcessMicroservice() {
    Microservice microservice = new Microservice();
    manager.processMicroserviceWithAdapters(microservice);

    Assertions.assertEquals("order=0", microservice.getProperties().get("cas_env_one"));
    Assertions.assertEquals("order=0", microservice.getProperties().get("cas_env_two"));
    Assertions.assertNull(microservice.getProperties().get("default-env-adapter"));
  }

  @Test
  public void testProcessInstance() {
    MicroserviceInstance instance = new MicroserviceInstance();
    manager.processInstanceWithAdapters(instance);

    Assertions.assertEquals("order=0", instance.getProperties().get("cas_env_one"));
    Assertions.assertEquals("order=0", instance.getProperties().get("cas_env_two"));
    Assertions.assertNull(instance.getProperties().get("default-env-adapter"));
  }
}
