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

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.SchemaUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.swagger.models.Swagger;

@Component
public class ConsumerSchemaFactory extends AbstractSchemaFactory<ConsumerSchemaContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerSchemaFactory.class);

  // 允许consumerIntf与schemaId对应的interface原型不同，用于支持context类型的参数
  // consumerIntf为null，表示原型与契约相同
  public void createConsumerSchema(MicroserviceMeta microserviceMeta, Microservice microservice) {
    for (String schemaId : microservice.getSchemas()) {
      ConsumerSchemaContext context = new ConsumerSchemaContext();
      context.setMicroserviceMeta(microserviceMeta);
      context.setMicroservice(microservice);
      context.setSchemaId(schemaId);
      context.setProviderClass(null);

      getOrCreateSchema(context);
    }
  }

  @Override
  protected SchemaMeta createSchema(ConsumerSchemaContext context) {
    // 尝试从规划的目录或服务中心加载契约
    Swagger swagger = loadSwagger(context);

    // 注册契约
    return schemaLoader.registerSchema(context.getMicroserviceMeta(), context.getSchemaId(), swagger);
  }

  @Override
  protected Swagger loadSwagger(ConsumerSchemaContext context) {
    Swagger swagger = super.loadSwagger(context);
    if (swagger != null) {
      return swagger;
    }

    ServiceRegistryClient client = RegistryUtils.getServiceRegistryClient();
    String schemaContent = client.getSchema(context.getMicroservice().getServiceId(), context.getSchemaId());
    LOGGER.info("load schema from service center, microservice={}:{}:{}, schemaId={}, result={}",
        context.getMicroservice().getAppId(),
        context.getMicroservice().getServiceName(),
        context.getMicroservice().getVersion(),
        context.getSchemaId(),
        !StringUtils.isEmpty(schemaContent));
    LOGGER.debug(schemaContent);
    if (schemaContent != null) {
      return SchemaUtils.parseSwagger(schemaContent);
    }

    throw new Error(
        String.format("no schema in local, and can not get schema from service center, %s:%s",
            context.getMicroserviceName(),
            context.getSchemaId()));
  }
}
