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
import java.util.List;
import java.util.Map.Entry;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.exception.ExceptionUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

public class SchemaMeta extends CommonService<OperationMeta> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaMeta.class);

  // 如果要生成class，使用这个package
  private String packageName;

  private Swagger swagger;

  private MicroserviceMeta microserviceMeta;

  // microserviceName:schemaId
  private String microserviceQualifiedName;

  // 契约对应的接口
  private Class<?> swaggerIntf;

  // handlerChain是microservice级别的
  private List<Handler> consumerHandlerChain;

  private List<Handler> providerHandlerChain;

  public SchemaMeta(Swagger swagger, MicroserviceMeta microserviceMeta, String schemaId) {
    this.packageName = SchemaUtils.generatePackageName(microserviceMeta, schemaId);

    this.swagger = swagger;
    this.name = schemaId;

    this.microserviceMeta = microserviceMeta;
    this.microserviceQualifiedName = microserviceMeta.getName() + "." + schemaId;
    // 确保swagger对应的接口是存在的
    swaggerIntf = ClassUtils.getOrCreateInterface(swagger, microserviceMeta.getClassLoader(), packageName);

    createOperationMgr("schemaMeta " + schemaId + " operation mgr");
    operationMgr.setRegisterErrorFmt("Operation name repeat, schema=%s, operation=%s");

    initOperations();
  }

  public String getPackageName() {
    return packageName;
  }

  private void initOperations() {
    for (Entry<String, Path> entry : swagger.getPaths().entrySet()) {
      String strPath = entry.getKey();
      Path path = entry.getValue();
      for (Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
        Operation operation = operationEntry.getValue();
        if (operation.getOperationId() == null) {
          throw ExceptionUtils.operationIdInvalid(getSchemaId(), strPath);
        }

        // org.apache.servicecomb.swagger.engine.SwaggerEnvironment.createConsumer(Class<?>, Class<?>)
        // org.apache.servicecomb.swagger.engine.SwaggerEnvironment.createProducer(Object, Swagger)
        // had make sure that consumer/swagger or producer/swagger can work
        //
        // in this place, do not throw exception when method not exists
        // eg:
        //   swagger interface is a.b.c, and consumer interface is a.b.c too.
        //   version 1, there are the same
        //   version 2, producer add a new operation, that means swagger have more operation than consumer interface a.b.c
        //              interface a.b.c in consumer process is the old interface
        //              so for swagger, can not do any valid check here
        //              only need to save found method, that's enough.
        Method method = ReflectUtils.findMethod(swaggerIntf, operation.getOperationId());
        if (method == null) {
          LOGGER.warn("method {} not found in swagger interface {}, schemaId={}",
              operation.getOperationId(),
              swaggerIntf.getName(),
              getSchemaId());
          continue;
        }

        String httpMethod = operationEntry.getKey().name();
        OperationMeta operationMeta = new OperationMeta();
        operationMeta.init(this, method, strPath, httpMethod, operation);
        operationMgr.register(method.getName(), operationMeta);
      }
    }
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public String getSchemaId() {
    return name;
  }

  public String getMicroserviceQualifiedName() {
    return microserviceQualifiedName;
  }

  public String getMicroserviceName() {
    return microserviceMeta.getName();
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public Class<?> getSwaggerIntf() {
    return swaggerIntf;
  }

  public List<Handler> getConsumerHandlerChain() {
    return consumerHandlerChain;
  }

  public void setConsumerHandlerChain(List<Handler> consumerHandlerChain) {
    this.consumerHandlerChain = consumerHandlerChain;
  }

  public List<Handler> getProviderHandlerChain() {
    return providerHandlerChain;
  }

  public void setProviderHandlerChain(List<Handler> providerHandlerChain) {
    this.providerHandlerChain = providerHandlerChain;
  }
}
