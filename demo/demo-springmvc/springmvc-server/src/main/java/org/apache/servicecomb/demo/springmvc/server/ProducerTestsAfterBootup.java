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

package org.apache.servicecomb.demo.springmvc.server;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.registry.RegistrationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.netflix.config.DynamicPropertyFactory;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

/**
 * Testing after bootup.
 */
@Component
public class ProducerTestsAfterBootup implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerTestsAfterBootup.class);

  private ObjectWriter writer = Yaml.pretty();

  private static final String EXPECTED_DATA = "---\n"
      + "swagger: \"2.0\"\n"
      + "info:\n"
      + "  version: \"1.0.0\"\n"
      + "  title: \"swagger definition for org.apache.servicecomb.demo.springmvc.server.CodeFirstSpringmvcForSchema\"\n"
      + "  x-java-interface: \"gen.swagger.CodeFirstSpringmvcForSchemaIntf\"\n"
      + "basePath: \"/forScheam\"\n"
      + "consumes:\n"
      + "- \"application/json\"\n"
      + "produces:\n"
      + "- \"application/json\"\n"
      + "paths:\n"
      + "  /uploadFile:\n"
      + "    post:\n"
      + "      operationId: \"uploadAwardFile\"\n"
      + "      consumes:\n"
      + "      - \"multipart/form-data\"\n"
      + "      produces:\n"
      + "      - \"application/json\"\n"
      + "      parameters:\n"
      + "      - name: \"fileType\"\n"
      + "        in: \"query\"\n"
      + "        required: true\n"
      + "        type: \"string\"\n"
      + "      - name: \"zoneId\"\n"
      + "        in: \"query\"\n"
      + "        required: true\n"
      + "        type: \"string\"\n"
      + "      - name: \"file\"\n"
      + "        in: \"formData\"\n"
      + "        required: true\n"
      + "        type: \"file\"\n"
      + "      responses:\n"
      + "        \"200\":\n"
      + "          description: \"response of 200\"\n"
      + "          schema:\n"
      + "            type: \"boolean\"\n";

  public void testSchemaNotChange(SCBEngine scbEngine) {
    LOGGER.info("ProducerTestsAfterBootup testing start");
    //we can not set microserviceName any more
    SchemaMeta meta = scbEngine.getProducerProviderManager().registerSchema("test1", new CodeFirstSpringmvcForSchema());
    String codeFirst = getSwaggerContent(meta.getSwagger());
    TestMgr.check(EXPECTED_DATA,
        codeFirst);
  }

  public void testRegisteredBasePath() {
    if (DynamicPropertyFactory.getInstance().getBooleanProperty("servicecomb.test.vert.transport", true).get()) {
      TestMgr.check(17, RegistrationManager.INSTANCE.getMicroservice().getPaths().size());
    } else {
      TestMgr.check(18, RegistrationManager.INSTANCE.getMicroservice().getPaths().size());
    }
  }

  private String getSwaggerContent(Swagger swagger) {
    try {
      return writer.writeValueAsString(swagger);
    } catch (JsonProcessingException e) {
      throw new Error(e);
    }
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (event.getEventType() == BootListener.EventType.AFTER_REGISTRY) {
      testSchemaNotChange(event.getScbEngine());
      testRegisteredBasePath();
      if (!TestMgr.isSuccess()) {
        TestMgr.summary();
        throw new IllegalStateException("some tests are failed. ");
      }
    }
  }
}

