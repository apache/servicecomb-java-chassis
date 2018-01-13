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
package org.apache.servicecomb.serviceregistry.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceRegisterTask {
  private EventBus eventBus;

  private Microservice microservice;

  private List<MicroserviceRegisterTask> taskList;

  @Before
  public void setup() {
    eventBus = new EventBus();

    taskList = new ArrayList<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onEvent(MicroserviceRegisterTask task) {
        taskList.add(task);
      }
    });

    microservice = new Microservice();
    microservice.setAppId("app");
    microservice.setServiceName("ms");

    microservice.setInstance(new MicroserviceInstance());
  }

  @Test
  public void testNewRegisterFailed(@Mocked ServiceRegistryClient srClient) {
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = null;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(false, registerTask.isRegistered());
    Assert.assertEquals(false, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals(null, microservice.getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testNewRegisterSuccess(@Mocked ServiceRegistryClient srClient) {
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = "serviceId";
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals("serviceId", microservice.getInstance().getServiceId());
    Assert.assertEquals(1, taskList.size());

    registerTask.run();
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testRegisterSchemaFailed(@Mocked ServiceRegistryClient srClient) {
    microservice.addSchema("s1", "");
    microservice.addSchema("exist", "");
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = "serviceId";
        srClient.isSchemaExist("serviceId", "exist");
        result = true;
        srClient.isSchemaExist(anyString, anyString);
        result = false;
        srClient.registerSchema(anyString, anyString, anyString);
        result = false;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(false, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals("serviceId", microservice.getInstance().getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testRegisterSchemaSuccess(@Mocked ServiceRegistryClient srClient) {
    microservice.addSchema("s1", "");
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = "serviceId";
        srClient.isSchemaExist(anyString, anyString);
        result = false;
        srClient.registerSchema(anyString, anyString, anyString);
        result = true;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals("serviceId", microservice.getInstance().getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testAlreadyRegisteredSchemaIdSetMatch(@Mocked ServiceRegistryClient srClient) {
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = microservice;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals("serviceId", microservice.getInstance().getServiceId());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testAlreadyRegisteredSchemaIdSetNotMatch(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "");

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(false, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals("serviceId", microservice.getInstance().getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testAlreadyRegisteredGetSchemaIdSetFailed(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "");

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = null;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(false, registerTask.isRegistered());
    Assert.assertEquals(false, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals(1, taskList.size());
  }
}
