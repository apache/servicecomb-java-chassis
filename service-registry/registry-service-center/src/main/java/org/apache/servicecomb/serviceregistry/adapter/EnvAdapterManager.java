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

import java.util.List;

import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvAdapterManager extends RegisterManager<String, EnvAdapter> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnvAdapterManager.class);

  private static final List<EnvAdapter> ENV_ADAPTERS = SPIServiceUtils.getSortedService(EnvAdapter.class);

  private static final String NAME = "service env adapter";

  public static final EnvAdapterManager INSTANCE = new EnvAdapterManager();

  private EnvAdapterManager() {
    super(NAME);
    ENV_ADAPTERS.forEach(envAdapter -> {
      if (envAdapter.enabled()) {
        getObjMap().putIfAbsent(envAdapter.getEnvName(), envAdapter);
      }
    });
  }

  public void processMicroserviceWithAdapters(Microservice microservice) {
    getObjMap().forEach((name, adapter) -> {
      LOGGER.info("Start process microservice with adapter {}", name);
      adapter.beforeRegisterService(microservice);
    });
  }

  public void processSchemaWithAdapters(String schemaId, String content) {
    getObjMap().forEach((name, adapter) -> {
      LOGGER.info("Start process schema with adapter {}", name);
      adapter.beforeRegisterSchema(schemaId, content);
    });
  }

  public void processInstanceWithAdapters(MicroserviceInstance instance) {
    getObjMap().forEach((name, adapter) -> {
      LOGGER.info("Start process instance with adapter {}", name);
      adapter.beforeRegisterInstance(instance);
    });
  }
}
