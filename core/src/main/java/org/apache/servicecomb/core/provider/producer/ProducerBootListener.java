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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.foundation.common.utils.IOUtils;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

public class ProducerBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerBootListener.class);

  @Override
  public void onAfterTransport(BootEvent event) {
    // register schema to microservice;
    Microservice microservice = RegistrationManager.INSTANCE.getMicroservice();

    String swaggerSchema = "http";
    for (String endpoint : microservice.getInstance().getEndpoints()) {
      if (endpoint.startsWith("rest://") && endpoint.indexOf("sslEnabled=true") > 0) {
        swaggerSchema = "https";
        break;
      }
    }

    MicroserviceMeta microserviceMeta = event.getScbEngine().getProducerMicroserviceMeta();
    for (SchemaMeta schemaMeta : microserviceMeta.getSchemaMetas().values()) {
      Swagger swagger = schemaMeta.getSwagger();
      swagger.addScheme(Scheme.forValue(swaggerSchema));
      String content = SwaggerUtils.swaggerToString(swagger);
      LOGGER.info("generate swagger for {}/{}/{}, swagger: {}",
          microserviceMeta.getAppId(),
          microserviceMeta.getMicroserviceName(),
          schemaMeta.getSchemaId(),
          content);

      RegistrationManager.INSTANCE.addSchema(schemaMeta.getSchemaId(), content);
    }

    saveBasePaths(microserviceMeta);
  }

  // just compatible to old 3rd components， servicecomb not use it......
  private void saveBasePaths(MicroserviceMeta microserviceMeta) {
    if (!DynamicPropertyFactory.getInstance().getBooleanProperty(DefinitionConst.REGISTER_SERVICE_PATH, false).get()) {
      return;
    }

    String urlPrefix = ClassLoaderScopeContext.getClassLoaderScopeProperty(DefinitionConst.URL_PREFIX);
    Map<String, BasePath> basePaths = new LinkedHashMap<>();
    for (SchemaMeta schemaMeta : microserviceMeta.getSchemaMetas().values()) {
      Swagger swagger = schemaMeta.getSwagger();

      String basePath = swagger.getBasePath();
      if (StringUtils.isNotEmpty(urlPrefix) && !basePath.startsWith(urlPrefix)) {
        basePath = urlPrefix + basePath;
      }
      if (StringUtils.isNotEmpty(basePath)) {
        BasePath basePathObj = new BasePath();
        basePathObj.setPath(basePath);
        basePaths.put(basePath, basePathObj);
      }
    }

    RegistrationManager.INSTANCE.addBasePath(basePaths.values());
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
}
