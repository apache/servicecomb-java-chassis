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
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;

import io.swagger.models.Operation;

public class OperationMeta {
  private SchemaMeta schemaMeta;

  // schemaId.operation
  private String schemaQualifiedName;

  // microserviceName.schemaId.operation
  private String microserviceQualifiedName;

  private String httpMethod;

  private String operationPath;

  private Operation swaggerOperation;

  private Map<String, Integer> parameterNameToIndexMap = new HashMap<>();

  // run in this executor
  private Executor executor;

  private ResponsesMeta responsesMeta = new ResponsesMeta();

  private OperationConfig config;

  private VendorExtensions vendorExtensions = new VendorExtensions();

  public OperationMeta init(SchemaMeta schemaMeta, SwaggerOperation swaggerOperation) {
    this.schemaMeta = schemaMeta;
    this.schemaQualifiedName = schemaMeta.getSchemaId() + "." + swaggerOperation.getOperationId();
    this.microserviceQualifiedName =
        schemaMeta.getMicroserviceQualifiedName() + "." + swaggerOperation.getOperationId();
    this.httpMethod = swaggerOperation.getHttpMethod().name();
    this.operationPath = swaggerOperation.getPath();
    this.swaggerOperation = swaggerOperation.getOperation();
    this.executor = schemaMeta.getMicroserviceMeta().getScbEngine().getExecutorManager().findExecutor(this);
    this.config = schemaMeta.getMicroserviceMeta().getMicroserviceVersionsMeta().getOrCreateOperationConfig(this);
    this.responsesMeta.init(schemaMeta.getSwagger(), swaggerOperation.getOperation());

    buildParameterNameToIndex();

    return this;
  }

  private void buildParameterNameToIndex() {
    for (int idx = 0; idx < swaggerOperation.getParameters().size(); idx++) {
      parameterNameToIndexMap.put(swaggerOperation.getParameters().get(idx).getName(), idx);
    }
  }

  public OperationConfig getConfig() {
    return config;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getOperationPath() {
    return operationPath;
  }

  public Operation getSwaggerOperation() {
    return swaggerOperation;
  }

  public ResponsesMeta getResponsesMeta() {
    return responsesMeta;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return schemaMeta.getMicroserviceMeta();
  }

  public SchemaMeta getSchemaMeta() {
    return schemaMeta;
  }

  public String getSchemaQualifiedName() {
    return schemaQualifiedName;
  }

  public String getMicroserviceQualifiedName() {
    return microserviceQualifiedName;
  }

  public String getMicroserviceName() {
    return schemaMeta.getMicroserviceName();
  }

  public String getSchemaId() {
    return schemaMeta.getSchemaId();
  }

  public String getOperationId() {
    return swaggerOperation.getOperationId();
  }

  // invoker make sure idx is valid
  public String getParamName(int idx) {
    return swaggerOperation.getParameters().get(idx).getName();
  }

  public Integer getParameterIndex(String parameterName) {
    return parameterNameToIndexMap.get(parameterName);
  }

  public void putExtData(String key, Object data) {
    vendorExtensions.put(key, data);
  }

  public <T> T getExtData(String key) {
    return vendorExtensions.get(key);
  }

  public VendorExtensions getVendorExtensions() {
    return vendorExtensions;
  }

  public Executor getExecutor() {
    return executor;
  }

  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  public int getParamSize() {
    return swaggerOperation.getParameters().size();
  }

  /**
   * Only for JavaChassis internal usage.
   */
  @Deprecated
  public Handler getProviderQpsFlowControlHandler() {
    return getMicroserviceMeta().getProviderQpsFlowControlHandler();
  }
}
