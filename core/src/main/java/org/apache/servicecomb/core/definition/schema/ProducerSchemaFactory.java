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

package org.apache.servicecomb.core.definition.schema;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

@Component
public class ProducerSchemaFactory extends AbstractSchemaFactory<ProducerSchemaContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProducerSchemaFactory.class);

  @Inject
  private SwaggerEnvironment swaggerEnv;

  private ObjectWriter writer = Yaml.pretty();

  private String getSwaggerContent(Swagger swagger) {
    try {
      return writer.writeValueAsString(swagger);
    } catch (JsonProcessingException e) {
      throw new Error(e);
    }
  }

  public void setSwaggerEnv(SwaggerEnvironment swaggerEnv) {
    this.swaggerEnv = swaggerEnv;
  }

  // 只会在启动流程中调用
  public SchemaMeta getOrCreateProducerSchema(String microserviceName, String schemaId,
      Class<?> producerClass,
      Object producerInstance) {
    MicroserviceMeta microserviceMeta = microserviceMetaManager.getOrCreateMicroserviceMeta(microserviceName);

    ProducerSchemaContext context = new ProducerSchemaContext();
    context.setMicroserviceMeta(microserviceMeta);
    context.setSchemaId(schemaId);
    context.setProviderClass(producerClass);
    context.setProducerInstance(producerInstance);

    SchemaMeta schemaMeta = getOrCreateSchema(context);

    SwaggerProducer producer = swaggerEnv.createProducer(producerInstance, schemaMeta.getSwagger());
    Executor reactiveExecutor = BeanUtils.getBean(ExecutorManager.EXECUTOR_REACTIVE);
    for (OperationMeta operationMeta : schemaMeta.getOperations()) {
      SwaggerProducerOperation producerOperation = producer.findOperation(operationMeta.getOperationId());
      operationMeta.putExtData(Const.PRODUCER_OPERATION, producerOperation);

      if (CompletableFuture.class.equals(producerOperation.getProducerMethod().getReturnType())) {
        operationMeta.setExecutor(ExecutorManager.findExecutor(operationMeta, reactiveExecutor));
      }
    }

    return schemaMeta;
  }

  protected SchemaMeta createSchema(ProducerSchemaContext context) {
    // 尝试从规划的目录加载契约
    Swagger swagger = loadSwagger(context);
    if (swagger == null) {
      Set<String> combinedNames = RegistryUtils.getServiceRegistry().getCombinedMicroserviceNames();
      for (String name : combinedNames) {
        swagger = loadSwagger(name, context.getSchemaId());
        if (swagger != null) {
          break;
        }
      }
    }

    // 根据class动态产生契约
    SwaggerGenerator generator = generateSwagger(context);
    if (swagger == null) {
      swagger = generator.getSwagger();
      String swaggerContent = getSwaggerContent(swagger);
      LOGGER.info("generate swagger for {}/{}/{}, swagger: {}",
          context.getMicroserviceMeta().getAppId(),
          context.getMicroserviceName(),
          context.getSchemaId(),
          swaggerContent);
    }

    // 注册契约
    return schemaLoader.registerSchema(context.getMicroserviceMeta(), context.getSchemaId(), swagger);
  }
}
