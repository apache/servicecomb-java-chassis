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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

/**
 * Created by Administrator on 2018/6/22.
 */
public class ProducerSchemaFactoryHolder implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerSchemaFactoryHolder.class);

  private ObjectWriter writer = Yaml.pretty();

  @Autowired
  private ProducerSchemaFactory factory;

  public void test() {
    LOGGER.info("ProducerSchemaFactoryHolder testing start");
    SchemaMeta meta =
        factory.getOrCreateProducerSchema("customer-service",
            "test1",
            CodeFirstSpringmvcForSchema.class,
            new CodeFirstSpringmvcForSchema());
    String codeFirst = getSwaggerContent(meta.getSwagger());
    TestMgr.check("07a48acef4cc1a7f2387d695923c49e98951a974e4f51cf1356d6878db48888f",
        Hashing.sha256().newHasher().putString(codeFirst, Charsets.UTF_8).hash().toString());
    TestMgr.check(codeFirst.length(), 899);

    if (!TestMgr.isSuccess()) {
      TestMgr.summary();
      throw new IllegalStateException("schema not the same. ");
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
      test();
    }
  }
}

