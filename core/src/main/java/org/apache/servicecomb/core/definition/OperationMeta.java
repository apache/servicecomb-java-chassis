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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;

public class OperationMeta {
  private SchemaMeta schemaMeta;

  // schemaId.operation
  private String schemaQualifiedName;

  // microserviceName.schemaId.operation
  private String microserviceQualifiedName;

  private String httpMethod;

  private String operationPath;

  private Operation swaggerOperation;

  private Map<String, Type> swaggerArgumentsTypes = new HashMap<>();

  private Map<String, JavaType> argumentsTypes = new HashMap<>();

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

    return this;
  }

  private void buildArgumentsTypes() {
    this.argumentsTypes.clear();
    if (getSwaggerProducerOperation() != null) {
      SwaggerProducerOperation swaggerProducerOperation = getSwaggerProducerOperation();
      swaggerProducerOperation.getMethodParameterTypesBySwaggerName().forEach((k, v) -> {
        this.argumentsTypes.put(k, TypeFactory.defaultInstance().constructType(v));
      });
    }
  }

  private void buildSwaggerArgumentsTypes() {
    this.swaggerArgumentsTypes.clear();

    // TODO : WEAK handle BEAN query param and POJO wrapped arguments.
    if (getSwaggerProducerOperation() != null) {
      SwaggerProducerOperation swaggerProducerOperation = getSwaggerProducerOperation();
      for (Parameter parameter : swaggerOperation.getParameters()) {
        swaggerArgumentsTypes
            .put(parameter.getName(), swaggerProducerOperation.getSwaggerParameterType(parameter.getName()));
      }
    } else {
      for (Parameter parameter : swaggerOperation.getParameters()) {
        swaggerArgumentsTypes.put(parameter.getName(), Object.class);
      }
    }
  }

  public Type getSwaggerArgumentType(String name) {
    return this.swaggerArgumentsTypes.get(name);
  }

  public JavaType getArgumentType(String name) {
    return this.argumentsTypes.get(name) == null ? TypeFactory.defaultInstance().constructType(Object.class)
        : this.argumentsTypes.get(name);
  }

  public boolean isPojoWrappedArguments(String name) {
    if (this.getSwaggerProducerOperation() != null) {
      return this.getSwaggerProducerOperation().isPojoWrappedArguments(name);
    }
    return false;
  }

  public void setSwaggerProducerOperation(SwaggerProducerOperation swaggerProducerOperation) {
    this.putExtData(Const.PRODUCER_OPERATION, swaggerProducerOperation);
    buildArgumentsTypes();
    buildSwaggerArgumentsTypes();
  }

  public SwaggerProducerOperation getSwaggerProducerOperation() {
    return (SwaggerProducerOperation) this.getExtData(Const.PRODUCER_OPERATION);
  }

  public void setSwaggerConsumerOperation(SwaggerConsumerOperation swaggerConsumerOperation) {
    this.putExtData(Const.CONSUMER_OPERATION, swaggerConsumerOperation);
    buildSwaggerArgumentsTypes();
  }

  public SwaggerConsumerOperation getSwaggerConsumerOperation() {
    return (SwaggerConsumerOperation) this.getExtData(Const.CONSUMER_OPERATION);
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

  public void putExtData(String key, Object data) {
    vendorExtensions.put(key, data);
  }

  public <T> T getExtData(String key) {
    return vendorExtensions.get(key);
  }

  public Executor getExecutor() {
    return executor;
  }

  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  /**
   * Only for JavaChassis internal usage.
   */
  @Deprecated
  public Handler getProviderQpsFlowControlHandler() {
    return getMicroserviceMeta().getProviderQpsFlowControlHandler();
  }
}
