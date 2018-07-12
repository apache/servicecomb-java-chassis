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
import java.util.stream.Collectors;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.GetSchemaResponse;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.Holder;
import org.junit.After;
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

  LogCollector collector;

  @Before
  public void setup() {
    collector = new LogCollector();
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

  @After
  public void teardown() {
    collector.teardown();
  }

  @Test
  public void testNewRegisterFailed(@Mocked ServiceRegistryClient srClient) {
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
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
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setStatusCode(200);
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = "serviceId";
        srClient.getSchemas("serviceId");
        this.result = onlineSchemasHolder;
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

  /**
   * Local schemaId set is consistent with online schemaId set, and schema contents are not registered.
   * This service instance try to register schema content but failed.
   */
  @Test
  public void testRegisterSchemaFailed(@Mocked ServiceRegistryClient srClient) {
    microservice.addSchema("s1", "");
    microservice.addSchema("exist", "");
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setStatusCode(200);
    ArrayList<GetSchemaResponse> schemaResponses = new ArrayList<>();
    onlineSchemasHolder.setValue(schemaResponses);
    GetSchemaResponse schemaResponse = new GetSchemaResponse();
    schemaResponse.setSchemaId("s1");
    schemaResponses.add(schemaResponse);
    schemaResponse.setSchemaId("exist");
    schemaResponses.add(schemaResponse);
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = "serviceId";
        srClient.registerSchema(anyString, anyString, anyString);
        result = false;
        srClient.getSchemas("serviceId");
        this.result = onlineSchemasHolder;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(false, registerTask.isRegistered());
    Assert.assertEquals(false, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals("serviceId", microservice.getInstance().getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  /**
   * There is no microservice information in service center.
   */
  @Test
  public void testRegisterSchemaSuccess(@Mocked ServiceRegistryClient srClient) {
    microservice.addSchema("s1", "s1Content");
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setStatusCode(200);
    ArrayList<GetSchemaResponse> schemaResponseList = new ArrayList<>();
    onlineSchemasHolder.setValue(schemaResponseList);
    GetSchemaResponse schemaResponse = new GetSchemaResponse();
    schemaResponseList.add(schemaResponse);
    schemaResponse.setSchemaId("s1");
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = null;
        srClient.registerMicroservice((Microservice) any);
        result = "serviceId";
        srClient.getSchema("serviceId", "s1");
        result = "s1Content";
        srClient.getSchemas("serviceId");
        result = onlineSchemasHolder;
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
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setStatusCode(200);
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = microservice;
        srClient.getSchemas("serviceId");
        result = onlineSchemasHolder;
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

  @Test(expected = IllegalStateException.class)
  public void testAlreadyRegisteredSchemaIdSetNotMatch(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "");
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setStatusCode(200);
    ArrayList<GetSchemaResponse> schemaResponseList = new ArrayList<>();
    onlineSchemasHolder.setValue(schemaResponseList);
    GetSchemaResponse schemaResponse = new GetSchemaResponse();

    schemaResponseList.add(schemaResponse);
    schemaResponse.setSchemaId("s1");
    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas("serviceId");
        result = onlineSchemasHolder;
      }
    };

    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();
  }

  @Test
  public void testAlreadyRegisteredGetSchemaIdSetFailed(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "");

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
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

  @Test
  public void testReRegisteredSetForDev(@Mocked ServiceRegistryClient srClient) {
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty("service_description.environment", "development");
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "");

    List<GetSchemaResponse> list = new ArrayList<>();
    GetSchemaResponse resp = new GetSchemaResponse();
    resp.setSchemaId("s1");
    resp.setSummary("c1188d709631a9038874f9efc6eb894f");
    list.add(resp);
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setStatusCode(200).setValue(list);

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas(anyString);
        result = onlineSchemasHolder;
        srClient.registerSchema(microservice.getServiceId(), anyString, anyString);
        result = true;
      }
    };

    microservice.addSchema("s1", "");
    microservice.setEnvironment("development");
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  /**
   * There is microservice information but no schema in service center.
   */
  @Test
  public void testFirstRegisterForProd(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", null);

    List<GetSchemaResponse> list = new ArrayList<>();
    GetSchemaResponse resp = new GetSchemaResponse();
    resp.setSchemaId("s1");
    list.add(resp);
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setValue(list).setStatusCode(200);

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas(anyString);
        result = onlineSchemasHolder;
        srClient.getSchema("serviceId", "s1");
        result = null;
        srClient.registerSchema("serviceId", "s1", "s1Content");
        result = true;
      }
    };

    microservice.addSchema("s1", "s1Content");
    microservice.setEnvironment("production");
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  /**
   * There is schema in service center which is different from local schema.
   */
  @Test(expected = IllegalStateException.class)
  public void testReRegisteredSetForProd(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "");

    List<GetSchemaResponse> list = new ArrayList<>();
    GetSchemaResponse resp = new GetSchemaResponse();
    resp.setSchemaId("s1");
    resp.setSummary("c1188d709631a9038874f9efc6eb894f");
    list.add(resp);
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setValue(list).setStatusCode(200);

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas(anyString);
        result = onlineSchemasHolder;
      }
    };

    microservice.addSchema("s1", "");
    microservice.setEnvironment("prod");
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();
  }

  /**
   * env = production and there are schemas only existing in service center
   */
  @Test(expected = IllegalStateException.class)
  public void testReRegisterForProductAndLocalSchemasAreLess(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", null);
    otherMicroservice.addSchema("s2", null);

    List<GetSchemaResponse> list = new ArrayList<>();
    GetSchemaResponse resp = new GetSchemaResponse();
    resp.setSchemaId("s1");
    list.add(resp);
    resp = new GetSchemaResponse();
    resp.setSchemaId("s2");
    list.add(resp);
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setValue(list).setStatusCode(200);

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas(anyString);
        result = onlineSchemasHolder;
        srClient.getSchema("serviceId", "s1");
        result = null;
        srClient.registerSchema("serviceId", "s1", "s1Content");
        result = true;
      }
    };

    microservice.addSchema("s1", "s1Content");
    microservice.setEnvironment("production");
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();
  }

  @Test
  public void testReRegisterForDevAndLocalSchemasAreLess(@Mocked ServiceRegistryClient srClient) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", null);
    otherMicroservice.addSchema("s2", null);

    List<GetSchemaResponse> list = new ArrayList<>();
    GetSchemaResponse resp = new GetSchemaResponse();
    resp.setSchemaId("s1");
    list.add(resp);
    resp = new GetSchemaResponse();
    resp.setSchemaId("s2");
    list.add(resp);
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setValue(list).setStatusCode(200);
    Holder<String> removeSchemaResult = new Holder<>();
    removeSchemaResult.setStatusCode(200);

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas(anyString);
        result = onlineSchemasHolder;
        srClient.getSchema("serviceId", "s1");
        result = null;
        srClient.registerSchema("serviceId", "s1", "s1Content");
        result = true;
      }
    };

    microservice.addSchema("s1", "s1Content");
    microservice.setEnvironment("development");
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(true, registerTask.isSchemaIdSetMatch());
    Assert.assertEquals("serviceId", microservice.getServiceId());
    Assert.assertEquals(1, taskList.size());
  }

  /**
   * There is schema in service center which is different from local schema.
   */
  @Test
  public void testLocalSchemaAndServiceCenterSchemaDiff(@Mocked ServiceRegistryClient srClient) {

    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");
    otherMicroservice.addSchema("s1", "abcd");

    List<GetSchemaResponse> list = new ArrayList<>();
    GetSchemaResponse resp = new GetSchemaResponse();
    resp.setSchemaId("s1");
    resp.setSummary("c1188d709631a9038874f9efc6eb894f");
    list.add(resp);
    Holder<List<GetSchemaResponse>> onlineSchemasHolder = new Holder<>();
    onlineSchemasHolder.setValue(list).setStatusCode(200);

    new Expectations() {
      {
        srClient.getMicroserviceId(anyString, anyString, anyString, anyString);
        result = "serviceId";
        srClient.getMicroservice(anyString);
        result = otherMicroservice;
        srClient.getSchemas(anyString);
        result = onlineSchemasHolder;
        srClient.getSchema(anyString, anyString);
        result = "swagger: \"2.0\"\n" +
            "info:\n" +
            "  version: \"1.0.0\"\n" +
            "  title: \"swagger definition for org.apache.servicecomb.demo.jaxrs.server.RequestClientTimeOut\"\n" +
            "  x-java-interface: \"cse.gen.jaxrstest.jaxrs.clientreqtimeout.RequestClientTimeOutIntf\"\n" +
            "basePath: \"/clientreqtimeout\"\n" +
            "consumes:\n" +
            "- \"application/json\"\n" +
            "produces:\n" +
            "- \"application/json\"\n" +
            "paths:\n" +
            "  /sayhello:\n" +
            "    post:\n" +
            "      operationId: \"sayHello\"\n" +
            "      parameters:\n" +
            "      - in: \"body\"\n" +
            "        name: \"student\"\n" +
            "        required: false\n" +
            "        schema:\n" +
            "          $ref: \"#/definitions/Student\"\n" +
            "      responses:\n" +
            "        200:\n" +
            "          description: \"response of 200\"\n" +
            "          schema:\n" +
            "            $ref: \"#/definitions/Student\"\n" +
            "definitions:\n" +
            "  Student:\n" +
            "    type: \"object\"\n" +
            "    required:\n" +
            "    - \"name\"\n" +
            "    properties:\n" +
            "      name:\n" +
            "        type: \"string\"\n" +
            "      age:\n" +
            "        type: \"integer\"\n" +
            "        format: \"int32\"\n" +
            "        maximum: 20\n" +
            "    x-java-class: \"org.apache.servicecomb.demo.validator.Student\"";
      }
    };

    microservice.addSchema("s1",
        "swagger: \"2.0\"\n" +
            "info:\n" +
            "  version: \"1.0.0\"\n" +
            "  title: \"swagger definition for org.apache.servicecomb.demo.jaxrs.server.RequestClientTimeOut\"\n" +
            "  x-java-interface: \"cse.gen.jaxrstest.jaxrs.clientreqtimeout.RequestClientTimeOutIntf\"\n" +
            "basePath: \"/clientreqtimeout\"\n" +
            "consumes:\n" +
            "- \"application/json\"\n" +
            "produces:\n" +
            "- \"application/json\"\n" +
            "paths:\n" +
            "  /sayhello:\n" +
            "    post:\n" +
            "      operationId: \"sayHello\"\n" +
            "      parameters:\n" +
            "      - in: \"body\"\n" +
            "        name: \"student\"\n" +
            "        required: false\n" +
            "        schema:\n" +
            "          $ref: \"#/definitions/Student\"\n" +
            "      responses:\n" +
            "        200:\n" +
            "          description: \"response of 200\"\n" +
            "          schema:\n" +
            "            type: \"string\"\n" +
            "definitions:\n" +
            "  Student:\n" +
            "    type: \"object\"\n" +
            "    required:\n" +
            "    - \"name\"\n" +
            "    properties:\n" +
            "      name:\n" +
            "        type: \"string\"\n" +
            "      age:\n" +
            "        type: \"integer\"\n" +
            "        format: \"int32\"\n" +
            "        maximum: 20\n" +
            "    x-java-class: \"org.apache.servicecomb.demo.validator.Student\"");
    microservice.setEnvironment("prod");
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);

    Boolean isIlleagalException = false;

    try {
      registerTask.run();
    } catch (IllegalStateException exception) {
      isIlleagalException = true;
      List<LoggingEvent> events = collector.getEvents().stream().filter(e -> {
        return MicroserviceRegisterTask.class.getName().equals(e.getLoggerName());
      }).collect(Collectors.toList());

      Assert.assertEquals("service center schema and local schema both are different:\n" +
          " service center schema:\n" +
          "[swagger: \"2.0\"\n" +
          "info:\n" +
          "  version: \"1.0.0\"\n" +
          "  title: \"swagger definition for org.apache.servicecomb.demo.jaxrs.server.RequestClientTimeOut\"\n" +
          "  x-java-interface: \"cse.gen.jaxrstest.jaxrs.clientreqtimeout.RequestClientTimeOutIntf\"\n" +
          "basePath: \"/clientreqtimeout\"\n" +
          "consumes:\n" +
          "- \"application/json\"\n" +
          "produces:\n" +
          "- \"application/json\"\n" +
          "paths:\n" +
          "  /sayhello:\n" +
          "    post:\n" +
          "      operationId: \"sayHello\"\n" +
          "      parameters:\n" +
          "      - in: \"body\"\n" +
          "        name: \"student\"\n" +
          "        required: false\n" +
          "        schema:\n" +
          "          $ref: \"#/definitions/Student\"\n" +
          "      responses:\n" +
          "        200:\n" +
          "          description: \"response of 200\"\n" +
          "          schema:\n" +
          "            $ref: \"#/definitions/Student\"\n" +
          "definitions:\n" +
          "  Student:\n" +
          "    type: \"object\"\n" +
          "    required:\n" +
          "    - \"name\"\n" +
          "    properties:\n" +
          "      name:\n" +
          "        type: \"string\"\n" +
          "      age:\n" +
          "        type: \"integer\"\n" +
          "        format: \"int32\"\n" +
          "        maximum: 20\n" +
          "    x-java-class: \"org.apache.servicecomb.demo.validator.Student\"\n" +
          " local schema:\n" +
          "[swagger: \"2.0\"\n" +
          "info:\n" +
          "  version: \"1.0.0\"\n" +
          "  title: \"swagger definition for org.apache.servicecomb.demo.jaxrs.server.RequestClientTimeOut\"\n" +
          "  x-java-interface: \"cse.gen.jaxrstest.jaxrs.clientreqtimeout.RequestClientTimeOutIntf\"\n" +
          "basePath: \"/clientreqtimeout\"\n" +
          "consumes:\n" +
          "- \"application/json\"\n" +
          "produces:\n" +
          "- \"application/json\"\n" +
          "paths:\n" +
          "  /sayhello:\n" +
          "    post:\n" +
          "      operationId: \"sayHello\"\n" +
          "      parameters:\n" +
          "      - in: \"body\"\n" +
          "        name: \"student\"\n" +
          "        required: false\n" +
          "        schema:\n" +
          "          $ref: \"#/definitions/Student\"\n" +
          "      responses:\n" +
          "        200:\n" +
          "          description: \"response of 200\"\n" +
          "          schema:\n" +
          "            type: \"string\"\n" +
          "definitions:\n" +
          "  Student:\n" +
          "    type: \"object\"\n" +
          "    required:\n" +
          "    - \"name\"\n" +
          "    properties:\n" +
          "      name:\n" +
          "        type: \"string\"\n" +
          "      age:\n" +
          "        type: \"integer\"\n" +
          "        format: \"int32\"\n" +
          "        maximum: 20\n" +
          "    x-java-class: \"org.apache.servicecomb.demo.validator.Student\"]", events.get(4).getMessage());

      Assert.assertEquals("The difference in local schema:\n" +
          "[type: \"string\"\n" +
          "definitions:\n" +
          "  Student:\n" +
          "    type: \"object\"\n" +
          "    required:\n" +
          "    - \"name\"\n" +
          "    properties:\n" +
          "      name:\n" +
          "        type: \"string\"\n" +
          "      age:\n" +
          "        type: \"integer\"\n" +
          "        format: \"int32\"\n" +
          "        maximum: 20\n" +
          "    x-java-class: \"org.apache.servicecomb.demo.validator.Student\"]", events.get(5).getMessage());

    }

    Assert.assertEquals(true, isIlleagalException);
  }
}
