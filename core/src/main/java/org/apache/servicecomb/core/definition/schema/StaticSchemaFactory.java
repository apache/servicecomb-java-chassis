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
import org.apache.servicecomb.serviceregistry.api.registry.StaticMicroservice;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.models.Swagger;

@Component
public class StaticSchemaFactory extends AbstractSchemaFactory<SchemaContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StaticSchemaFactory.class);

  public void loadSchema(MicroserviceMeta microserviceMeta, StaticMicroservice microservice) {
    SchemaContext context = new SchemaContext();
    context.setMicroserviceMeta(microserviceMeta);
    // use microservice name as schemaId, since currently we only allow one schema per 3rd party microservice
    context.setSchemaId(microserviceMeta.getShortName());
    context.setProviderClass(microservice.getSchemaIntfCls());

    getOrCreateSchema(context);
  }

  @Override
  protected SchemaMeta createSchema(SchemaContext context) {
    // generate schema according to producer interface
    Swagger swagger;
    SwaggerGenerator generator = generateSwagger(context);
    swagger = generator.getSwagger();
    String swaggerContent = SwaggerUtils.swaggerToString(swagger);
    LOGGER.info("generate swagger for {}/{}/{}, swagger: {}",
        context.getMicroserviceMeta().getAppId(),
        context.getMicroserviceName(),
        context.getSchemaId(),
        swaggerContent);

    // register swagger schema
    return schemaLoader.registerSchema(context.getMicroserviceMeta(), context.getSchemaId(), swagger);
  }
}
