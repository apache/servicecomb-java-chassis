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

package org.apache.servicecomb.core.provider.producer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.utils.IOUtils;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.OpenAPI;

public class ProducerBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerBootListener.class);

  private static final String PATTERN = File.separator + "microservices"
      + File.separator + "%s" + File.separator + "%s.yaml";

  private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

  private RegistrationManager registrationManager;

  private MicroserviceProperties microserviceProperties;

  private Environment environment;

  @Autowired
  public void setRegistrationManager(RegistrationManager registrationManager) {
    this.registrationManager = registrationManager;
  }

  @Autowired
  public void setMicroserviceProperties(MicroserviceProperties microserviceProperties) {
    this.microserviceProperties = microserviceProperties;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void onAfterTransport(BootEvent event) {
    boolean exportToFile = environment.getProperty(CoreConst.SWAGGER_EXPORT_ENABLED, boolean.class, true);
    String filePath = environment.getProperty(CoreConst.SWAGGER_DIRECTORY, String.class, TMP_DIR) + PATTERN;

    if (exportToFile) {
      LOGGER.info("export microservice swagger file to path {}", filePath);
    }
    // register schema to microservice;
    MicroserviceMeta microserviceMeta = event.getScbEngine().getProducerMicroserviceMeta();
    for (SchemaMeta schemaMeta : microserviceMeta.getSchemaMetas().values()) {
      OpenAPI swagger = schemaMeta.getSwagger();
      String content = SwaggerUtils.swaggerToString(swagger);
      if (exportToFile) {
        exportToFile(String.format(filePath, microserviceProperties.getName(), schemaMeta.getSchemaId()), content);
      } else {
        LOGGER.info("generate swagger for {}/{}/{}, swagger: {}",
            microserviceMeta.getAppId(),
            microserviceMeta.getMicroserviceName(),
            schemaMeta.getSchemaId(),
            content);
      }
      this.registrationManager.addSchema(schemaMeta.getSchemaId(), content);
    }
  }


  // bug: can not close all thread for edge
  @Override
  public void onAfterClose(BootEvent event) {
    MicroserviceMeta microserviceMeta = event.getScbEngine().getProducerMicroserviceMeta();
    if (microserviceMeta == null) {
      return;
    }

    for (OperationMeta operationMeta : microserviceMeta.getOperations()) {
      if (operationMeta.getExecutor() instanceof ExecutorService) {
        ((ExecutorService) operationMeta.getExecutor()).shutdown();
        continue;
      }

      if (operationMeta.getExecutor() instanceof Closeable) {
        IOUtils.closeQuietly((Closeable) operationMeta.getExecutor());
        continue;
      }

      LOGGER.warn("Executor {} do not support close or shutdown, it may block service shutdown.",
          operationMeta.getExecutor().getClass().getName());
    }
  }

  private void exportToFile(String fileName, String content) {
    File file = new File(fileName);
    if (!file.getParentFile().exists()) {
      if (!file.getParentFile().mkdirs()) {
        LOGGER.error("create file directory failed");
        return;
      }
    }
    if (file.exists()) {
      file.delete();
    }
    try {
      file.createNewFile();
      FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8, false);
      file.setReadOnly();
    } catch (IOException e) {
      LOGGER.error("export swagger content to file failed, message: {}", e.getMessage());
    }
  }
}
