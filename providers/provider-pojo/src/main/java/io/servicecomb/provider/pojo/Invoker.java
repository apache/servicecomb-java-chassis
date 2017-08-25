/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.pojo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.util.StringUtils;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import io.servicecomb.swagger.engine.SwaggerConsumer;
import io.servicecomb.swagger.engine.SwaggerConsumerOperation;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;

public class Invoker implements InvocationHandler {
  // 原始数据
  private String microserviceName;

  private String schemaId;

  private Class<?> consumerIntf;

  // 生成的数据
  private SchemaMeta schemaMeta;

  private ReferenceConfig referenceConfig;

  private SwaggerConsumer swaggerConsumer;

  public Invoker(String microserviceName, String schemaId, Class<?> consumerIntf) {
    this.microserviceName = microserviceName;
    this.schemaId = schemaId;
    this.consumerIntf = consumerIntf;
  }

  protected void prepare() {
    referenceConfig = ReferenceConfigUtils.getForInvoke(microserviceName);
    MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();

    if (StringUtils.isEmpty(schemaId)) {
      // 未指定schemaId，看看consumer接口是否等于契约接口
      schemaMeta = microserviceMeta.findSchemaMeta(consumerIntf);
      if (schemaMeta == null) {
        // 尝试用consumer接口名作为schemaId
        schemaId = consumerIntf.getName();
        schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
      }
    } else {
      schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
    }

    this.swaggerConsumer = CseContext.getInstance().getSwaggerEnvironment().createConsumer(consumerIntf,
        schemaMeta.getSwaggerIntf());
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (swaggerConsumer == null) {
      prepare();
    }

    Invocation invocation =
        InvocationFactory.forConsumer(referenceConfig, schemaMeta, method.getName(), null);

    SwaggerConsumerOperation consumerOperation = swaggerConsumer.findOperation(method.getName());
    consumerOperation.getArgumentsMapper().toInvocation(args, invocation);

    Response response = InvokerUtils.innerSyncInvoke(invocation);
    if (response.isSuccessed()) {
      return consumerOperation.getResponseMapper().mapResponse(response);
    }

    throw ExceptionFactory.convertConsumerException(response.getResult());
  }
}
