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
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.schema.ProducerSchemaFactory;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

/**
 * Testing after bootup.
 */
@Component
public class ProducerTestsAfterBootup implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerTestsAfterBootup.class);

  private ObjectWriter writer = Yaml.pretty();

  @Autowired
  private ProducerSchemaFactory factory;

  public void testSchemaNotChange() {
    LOGGER.info("ProducerTestsAfterBootup testing start");
    //we can not set microserviceName any more
    SchemaMeta meta =
        factory.getOrCreateProducerSchema("test1",
            CodeFirstSpringmvcForSchema.class,
            new CodeFirstSpringmvcForSchema());
    String codeFirst = getSwaggerContent(meta.getSwagger());
    TestMgr.check("2986daa46b229ec125443122dd7b51ee9a64879f1750d0996f948ce0718685c7",
        RegistryUtils.calcSchemaSummary(codeFirst));
    TestMgr.check(codeFirst.length(), 889);
  }

  public void testRegisteredBasePath() {
    TestMgr.check(13, RegistryUtils.getMicroservice().getPaths().size());
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
      testSchemaNotChange();
      testRegisteredBasePath();
      if (!TestMgr.isSuccess()) {
        TestMgr.summary();
        throw new IllegalStateException("some tests are failed. ");
      }
    }
  }
}

