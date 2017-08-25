/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.definition.loader;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.servicecomb.core.Handler;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.SchemaUtils;
import io.servicecomb.core.handler.ConsumerHandlerManager;
import io.servicecomb.core.handler.ProducerHandlerManager;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.BasePath;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.swagger.models.Swagger;

@Component
public class SchemaLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaLoader.class);

  @Inject
  protected MicroserviceMetaManager microserviceMetaManager;

  public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
    this.microserviceMetaManager = microserviceMetaManager;
  }

  /**
   * resource的路径格式，至少是以这个形式结尾：schemaId.yaml
   */
  public SchemaMeta registerSchema(String microserviceName, Resource resource) {
    try {
      String schemaId = FilenameUtils.getBaseName(resource.getFilename());

      String swaggerContent = IOUtils.toString(resource.getURL());
      SchemaMeta schemaMeta = registerSchema(microserviceName, schemaId, swaggerContent);

      return schemaMeta;
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  public SchemaMeta registerSchema(String microserviceName, String schemaId, String swaggerContent) {
    Swagger swagger = SchemaUtils.parseSwagger(swaggerContent);
    if (swagger == null) {
      throw new Error(String.format("Parse the swagger for %s:%s failed", microserviceName, schemaId));
    }

    return registerSchema(microserviceName, schemaId, swagger);
  }

  public SchemaMeta registerSchema(String microserviceName, String schemaId,
      Swagger swagger) {
    MicroserviceMeta microserviceMeta = microserviceMetaManager.getOrCreateMicroserviceMeta(microserviceName);

    return registerSchema(microserviceMeta, schemaId, swagger);
  }

  public SchemaMeta registerSchema(MicroserviceMeta microserviceMeta, String schemaId,
      Swagger swagger) {
    String microserviceName = microserviceMeta.getName();
    LOGGER.info("register schema {}/{}/{}", microserviceMeta.getAppId(), microserviceName, schemaId);

    SchemaMeta schemaMeta = new SchemaMeta(swagger, microserviceMeta, schemaId);

    List<Handler> producerHandlerChain = ProducerHandlerManager.INSTANCE.getOrCreate(microserviceName);
    schemaMeta.setProviderHandlerChain(producerHandlerChain);

    List<Handler> consumerHandlerChain = ConsumerHandlerManager.INSTANCE.getOrCreate(microserviceName);
    schemaMeta.setConsumerHandlerChain(consumerHandlerChain);

    microserviceMeta.regSchemaMeta(schemaMeta);

    putSelfBasePathIfAbsent(microserviceName, swagger.getBasePath());

    return schemaMeta;
  }

  public void putSelfBasePathIfAbsent(String microserviceName, String basePath) {
    if (basePath == null || basePath.length() == 0) {
      return;
    }

    Microservice microservice = RegistryUtils.getMicroservice();
    if (!microservice.getServiceName().equals(microserviceName)) {
      return;
    }

    String urlPrefix = System.getProperty(Const.URL_PREFIX);
    if (!StringUtils.isEmpty(urlPrefix) && !basePath.startsWith(urlPrefix)) {
      basePath = urlPrefix + basePath;
    }

    List<BasePath> paths = microservice.getPaths();
    for (BasePath path : paths) {
      if (path.getPath().equals(basePath)) {
        return;
      }
    }

    BasePath basePathObj = new BasePath();
    basePathObj.setPath(basePath);
    paths.add(basePathObj);
  }
}
