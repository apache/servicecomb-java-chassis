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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.core.ProducerProvider;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.CoreMetaUtils;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;

public class ProducerProviderManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerProviderManager.class);

  private final List<ProducerProvider> producerProviderList = new ArrayList<>(
      SPIServiceUtils.getOrLoadSortedService(ProducerProvider.class));

  private final SCBEngine scbEngine;

  private final List<ProducerMeta> producerMetas = new ArrayList<>();

  public ProducerProviderManager(SCBEngine scbEngine) {
    this.scbEngine = scbEngine;
  }

  public List<ProducerProvider> getProducerProviderList() {
    return producerProviderList;
  }

  public void init() {
    registerProducerMetas(producerMetas);

    for (ProducerProvider provider : producerProviderList) {
      List<ProducerMeta> producerMetas = provider.init();
      if (producerMetas == null) {
        LOGGER.warn("ProducerProvider {} not provide any producer.", provider.getClass().getName());
        continue;
      }

      registerProducerMetas(producerMetas);
    }
  }

  public void addProducerMeta(String schemaId, Object instance) {
    addProducerMeta(new ProducerMeta(schemaId, instance));
  }

  public void addProducerMeta(ProducerMeta producerMeta) {
    producerMetas.add(producerMeta);
  }

  private void registerProducerMetas(List<ProducerMeta> producerMetas) {
    for (ProducerMeta producerMeta : producerMetas) {
      registerSchema(producerMeta.getSchemaId(), producerMeta.getSchemaInterface(), producerMeta.getInstance());
    }
  }

  public SchemaMeta registerSchema(String schemaId, Object instance) {
    return registerSchema(schemaId, null, instance);
  }

  public SchemaMeta registerSchema(String schemaId, Class<?> schemaInterface, Object instance) {
    MicroserviceMeta producerMicroserviceMeta = scbEngine.getProducerMicroserviceMeta();
    SwaggerProducer swaggerProducer = scbEngine.getSwaggerEnvironment()
        .createProducer(instance, schemaInterface);
    OpenAPI swagger = swaggerProducer.getSwagger();
    registerUrlPrefixToSwagger(swagger);

    // register self OpenAPI to registry
    scbEngine.getOpenAPIRegistryManager()
        .registerOpenAPI(BootStrapProperties.readApplication(scbEngine.getEnvironment()),
            BootStrapProperties.readServiceName(scbEngine.getEnvironment()), schemaId, swagger);

    SchemaMeta schemaMeta = producerMicroserviceMeta.registerSchemaMeta(schemaId, swagger);
    schemaMeta.putExtData(CoreMetaUtils.SWAGGER_PRODUCER, swaggerProducer);
    Executor reactiveExecutor = scbEngine.getExecutorManager().findExecutorById(ExecutorManager.EXECUTOR_REACTIVE);
    for (SwaggerProducerOperation producerOperation : swaggerProducer.getAllOperations()) {
      OperationMeta operationMeta = schemaMeta.ensureFindOperation(producerOperation.getOperationId());
      operationMeta.setSwaggerProducerOperation(producerOperation);

      if (CompletableFuture.class.equals(producerOperation.getProducerMethod().getReturnType())) {
        operationMeta.setExecutor(scbEngine.getExecutorManager().findExecutor(operationMeta, reactiveExecutor));
      }
    }

    return schemaMeta;
  }

  // This is special requirement by users: When service deployed in tomcat,user want to use RestTemplate to
  // call REST service by the full url. e.g. restTemplate.getForObejct("cse://serviceName/root/prefix/health")
  // By default, user's do not need context prefix, e.g. restTemplate.getForObejct("cse://serviceName/health")
  private void registerUrlPrefixToSwagger(OpenAPI swagger) {
    String urlPrefix = ClassLoaderScopeContext.getClassLoaderScopeProperty(DefinitionConst.URL_PREFIX);
    if (!StringUtils.isEmpty(urlPrefix) && !SwaggerUtils.getBasePath(swagger).startsWith(urlPrefix)
        && scbEngine.getEnvironment().getProperty(DefinitionConst.REGISTER_URL_PREFIX, boolean.class, false)) {
      LOGGER.info("Add swagger base path prefix for {} with {}", SwaggerUtils.getBasePath(swagger), urlPrefix);
      SwaggerUtils.setBasePath(swagger, urlPrefix + SwaggerUtils.getBasePath(swagger));
    }
  }
}
