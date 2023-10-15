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
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Testing after bootup.
 */
@Component
public class ProducerTestsAfterBootup implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerTestsAfterBootup.class);

  private ObjectWriter writer = Yaml.pretty();

  public void testSchemaNotChange(SCBEngine scbEngine) {
    LOGGER.info("ProducerTestsAfterBootup testing start");
    //we can not set microserviceName any more
    SchemaMeta meta = scbEngine.getProducerProviderManager().registerSchema("test1", new CodeFirstSpringmvcForSchema());
    String codeFirst = getSwaggerContent(meta.getSwagger());

    String expectSchema = UnitTestSwaggerUtils.loadExpect("schemas/CodeFirstSpringmvcForSchema.yaml")
        .replace("\r\n", "\n").trim();
    int offset = expectSchema.indexOf("---\nopenapi: 3.0.1");
    if (offset > 0) {
      expectSchema = expectSchema.substring(offset + 4);
    }

    TestMgr.check(expectSchema.trim(), codeFirst.trim());
  }

  private String getSwaggerContent(OpenAPI swagger) {
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
      if (!TestMgr.isSuccess()) {
        TestMgr.summary();
        throw new IllegalStateException("some tests are failed. ");
      }
    }
  }
}

