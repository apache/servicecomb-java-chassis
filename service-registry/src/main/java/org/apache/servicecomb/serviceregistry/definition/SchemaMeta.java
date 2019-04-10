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
package org.apache.servicecomb.serviceregistry.definition;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;

import io.swagger.models.Swagger;

public class SchemaMeta {
  private MicroserviceMeta microserviceMeta;

  private Swagger swagger;

  private String schemaId;

  // microserviceName.schemaId
  private String microserviceQualifiedName;

  private Map<String, OperationMeta> operationMetas = new HashMap<>();

  private Map<String, Object> vendorExtensions = new ConcurrentHashMapEx<>();

  public SchemaMeta(MicroserviceMeta microserviceMeta, String schemaId, Swagger swagger) {
    this.microserviceMeta = microserviceMeta;
    this.schemaId = schemaId;
    this.swagger = swagger;
    this.microserviceQualifiedName = microserviceMeta.getMicroserviceName() + "." + schemaId;

    initOperationMetas();
  }

  private SchemaMeta initOperationMetas() {
    SwaggerOperations swaggerOperations = new SwaggerOperations(swagger);
    for (SwaggerOperation swaggerOperation : swaggerOperations.getOperations().values()) {
      operationMetas.put(swaggerOperation.getOperationId(),
          new OperationMeta()
              .microserviceMeta(microserviceMeta)
              .schemaMeta(this)
              .schemaQualifiedName(schemaId + "." + swaggerOperation.getOperationId())
              .microserviceQualifiedName(microserviceQualifiedName + "." + swaggerOperation.getOperationId())
              .httpMethod(swaggerOperation.getHttpMethod().name())
              .operationPath(swaggerOperation.getPath())
              .swaggerOperation(swaggerOperation.getOperation()));
    }
    return this;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public void setMicroserviceMeta(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
  }

  public SchemaMeta microserviceMeta(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
    return this;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public void setSwagger(Swagger swagger) {
    this.swagger = swagger;
  }

  public SchemaMeta swagger(Swagger swagger) {
    this.swagger = swagger;
    return this;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }

  public SchemaMeta schemaId(String schemaId) {
    this.schemaId = schemaId;
    return this;
  }

  public String getMicroserviceQualifiedName() {
    return microserviceQualifiedName;
  }

  public void setMicroserviceQualifiedName(String microserviceQualifiedName) {
    this.microserviceQualifiedName = microserviceQualifiedName;
  }

  public SchemaMeta microserviceQualifiedName(String microserviceQualifiedName) {
    this.microserviceQualifiedName = microserviceQualifiedName;
    return this;
  }

  public Map<String, OperationMeta> getOperationMetas() {
    return operationMetas;
  }

  public void setOperationMetas(Map<String, OperationMeta> operationMetas) {
    this.operationMetas = operationMetas;
  }

  public SchemaMeta operationMetas(Map<String, OperationMeta> operationMetas) {
    this.operationMetas = operationMetas;
    return this;
  }

  public Map<String, Object> getVendorExtensions() {
    return vendorExtensions;
  }
}
