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

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import io.swagger.models.Operation;

public class OperationMeta {
  private MicroserviceMeta microserviceMeta;

  private SchemaMeta schemaMeta;

  // schemaId.operation
  private String schemaQualifiedName;

  // microserviceName.schemaId.operation
  private String microserviceQualifiedName;

  private String httpMethod;

  private String operationPath;

  private Operation swaggerOperation;

  private Map<String, Object> vendorExtensions = new ConcurrentHashMapEx<>();

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public void setMicroserviceMeta(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
  }

  public OperationMeta microserviceMeta(MicroserviceMeta microserviceMeta) {
    this.microserviceMeta = microserviceMeta;
    return this;
  }

  public SchemaMeta getSchemaMeta() {
    return schemaMeta;
  }

  public void setSchemaMeta(SchemaMeta schemaMeta) {
    this.schemaMeta = schemaMeta;
  }

  public OperationMeta schemaMeta(SchemaMeta schemaMeta) {
    this.schemaMeta = schemaMeta;
    return this;
  }

  public String getSchemaQualifiedName() {
    return schemaQualifiedName;
  }

  public void setSchemaQualifiedName(String schemaQualifiedName) {
    this.schemaQualifiedName = schemaQualifiedName;
  }

  public OperationMeta schemaQualifiedName(String schemaQualifiedName) {
    this.schemaQualifiedName = schemaQualifiedName;
    return this;
  }

  public String getMicroserviceQualifiedName() {
    return microserviceQualifiedName;
  }

  public void setMicroserviceQualifiedName(String microserviceQualifiedName) {
    this.microserviceQualifiedName = microserviceQualifiedName;
  }

  public OperationMeta microserviceQualifiedName(String microserviceQualifiedName) {
    this.microserviceQualifiedName = microserviceQualifiedName;
    return this;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public OperationMeta httpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
    return this;
  }

  public String getOperationPath() {
    return operationPath;
  }

  public void setOperationPath(String operationPath) {
    this.operationPath = operationPath;
  }

  public OperationMeta operationPath(String operationPath) {
    this.operationPath = operationPath;
    return this;
  }

  public Operation getSwaggerOperation() {
    return swaggerOperation;
  }

  public void setSwaggerOperation(Operation swaggerOperation) {
    this.swaggerOperation = swaggerOperation;
  }

  public OperationMeta swaggerOperation(Operation swaggerOperation) {
    this.swaggerOperation = swaggerOperation;
    return this;
  }

  public Map<String, Object> getVendorExtensions() {
    return vendorExtensions;
  }
}
