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
package org.apache.servicecomb.swagger.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.swagger.models.Swagger;

public class SwaggerProducer {
  private Class<?> producerCls;

  private Object producerInstance;

  private Swagger swagger;

  // key is operationId
  private final Map<String, SwaggerProducerOperation> opMap = new HashMap<>();

  public Class<?> getProducerCls() {
    return producerCls;
  }

  public void setProducerCls(Class<?> producerCls) {
    this.producerCls = producerCls;
  }

  public Object getProducerInstance() {
    return producerInstance;
  }

  public void setProducerInstance(Object producerInstance) {
    this.producerInstance = producerInstance;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public void setSwagger(Swagger swagger) {
    this.swagger = swagger;
  }

  public void addOperation(SwaggerProducerOperation op) {
    opMap.put(op.getOperationId(), op);
  }

  public SwaggerProducerOperation findOperation(String operationId) {
    return opMap.get(operationId);
  }

  public Collection<SwaggerProducerOperation> getAllOperations() {
    return opMap.values();
  }
}
