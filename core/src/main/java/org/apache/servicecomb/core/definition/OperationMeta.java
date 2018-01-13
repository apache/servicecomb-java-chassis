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

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.swagger.invocation.response.ResponseMeta;
import org.apache.servicecomb.swagger.invocation.response.ResponsesMeta;

import io.swagger.models.Operation;

public class OperationMeta {
  private SchemaMeta schemaMeta;

  // schemaId:operation
  private String schemaQualifiedName;

  // microserviceName:schemaId:operation
  private String microserviceQualifiedName;

  // 契约对应的method，与consumer、producer的method没有必然关系
  private Method method;

  private String httpMethod;

  private String operationPath;

  private Operation swaggerOperation;

  // 在哪个executor上执行
  private Executor executor;

  private ResponsesMeta responsesMeta = new ResponsesMeta();

  // transport、provider、consumer端都可能需要扩展数据
  // 为避免每个地方都做复杂的层次管理，直接在这里保存扩展数据
  private Map<String, Object> extData = new ConcurrentHashMap<>();

  public void init(SchemaMeta schemaMeta, Method method, String operationPath, String httpMethod,
      Operation swaggerOperation) {
    this.schemaMeta = schemaMeta;
    schemaQualifiedName = schemaMeta.getSchemaId() + "." + method.getName();
    microserviceQualifiedName = schemaMeta.getMicroserviceName() + "." + schemaQualifiedName;
    this.operationPath = operationPath;
    this.method = method;
    this.httpMethod = httpMethod.toUpperCase(Locale.US);
    this.swaggerOperation = swaggerOperation;

    executor = ExecutorManager.findExecutor(this);

    responsesMeta.init(schemaMeta.getMicroserviceMeta().getClassLoader(),
        schemaMeta.getPackageName(),
        schemaMeta.getSwagger(),
        swaggerOperation,
        method.getGenericReturnType());
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getOperationPath() {
    return operationPath;
  }

  public Operation getSwaggerOperation() {
    return swaggerOperation;
  }

  public ResponseMeta findResponseMeta(int statusCode) {
    return responsesMeta.findResponseMeta(statusCode);
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

  public Method getMethod() {
    return method;
  }

  public String getOperationId() {
    return swaggerOperation.getOperationId();
  }

  // 调用者保证参数正确性
  public String getParamName(int idx) {
    return swaggerOperation.getParameters().get(idx).getName();
  }

  public void putExtData(String key, Object data) {
    extData.put(key, data);
  }

  @SuppressWarnings("unchecked")
  public <T> T getExtData(String key) {
    return (T) extData.get(key);
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
}
