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
package org.apache.servicecomb.core.definition;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Swagger;

public class SchemaMeta {
  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaMeta.class);

  private final MicroserviceMeta microserviceMeta;

  private final Swagger swagger;

  private final String schemaId;

  // microserviceName.schemaId
  private final String microserviceQualifiedName;

  private final Map<String, OperationMeta> operations = new HashMap<>();

  private final VendorExtensions vendorExtensions = new VendorExtensions();

  public SchemaMeta(MicroserviceMeta microserviceMeta, String schemaId, Swagger swagger) {
    this.microserviceMeta = microserviceMeta;
    this.schemaId = schemaId;
    this.swagger = swagger;
    this.microserviceQualifiedName = microserviceMeta.getMicroserviceName() + "." + schemaId;

    try {
      initOperationMetas();
    } catch (Throwable e) {
      LOGGER.error("Unhandled exception to {}.", microserviceQualifiedName, e);
      throw e;
    }
  }

  private SchemaMeta initOperationMetas() {
    SwaggerOperations swaggerOperations = new SwaggerOperations(swagger);
    for (SwaggerOperation swaggerOperation : swaggerOperations.getOperations().values()) {
      operations.put(swaggerOperation.getOperationId(), new OperationMeta().init(this, swaggerOperation));
    }
    return this;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public String getAppId() {
    return microserviceMeta.getAppId();
  }

  public String getMicroserviceName() {
    return microserviceMeta.getMicroserviceName();
  }

  public String getSchemaId() {
    return schemaId;
  }

  public String getMicroserviceQualifiedName() {
    return microserviceQualifiedName;
  }

  public Map<String, OperationMeta> getOperations() {
    return operations;
  }

  public void putExtData(String key, Object data) {
    vendorExtensions.put(key, data);
  }

  public <T> T getExtData(String key) {
    return vendorExtensions.get(key);
  }

  public OperationMeta findOperation(String operationId) {
    return operations.get(operationId);
  }

  public OperationMeta ensureFindOperation(String operationId) {
    OperationMeta value = operations.get(operationId);
    if (value == null) {
      throw new IllegalStateException(String
          .format("Can not find OperationMeta, microserviceName=%s, schemaId=%s, operationId=%s.",
              getMicroserviceName(), getSchemaId(), operationId));
    }

    return value;
  }
}
