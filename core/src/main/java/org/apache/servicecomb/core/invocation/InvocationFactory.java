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

package org.apache.servicecomb.core.invocation;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.serviceregistry.RegistryUtils;

public final class InvocationFactory {
  private InvocationFactory() {
  }

  private static String getMicroserviceName() {
    return RegistryUtils.getMicroservice().getServiceName();
  }

  public static Invocation forConsumer(ReferenceConfig referenceConfig, OperationMeta operationMeta,
      Object[] swaggerArguments) {
    Invocation invocation = new Invocation(referenceConfig,
        operationMeta,
        swaggerArguments);
    invocation.addContext(Const.SRC_MICROSERVICE, getMicroserviceName());
    return invocation;
  }

  /*
   * consumer端使用，schemaMeta级别的缓存，每次调用根据operationName来执行
   */
  public static Invocation forConsumer(ReferenceConfig referenceConfig, SchemaMeta schemaMeta, String operationName,
      Object[] swaggerArguments) {
    OperationMeta operationMeta = schemaMeta.ensureFindOperation(operationName);
    return forConsumer(referenceConfig, operationMeta, swaggerArguments);
  }

  /*
   * 为tcc场景提供的快捷方式,consumer端使用
   */
  public static Invocation forConsumer(ReferenceConfig referenceConfig, String operationQualifiedName,
      Object[] swaggerArguments) {
    MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();
    OperationMeta operationMeta = microserviceMeta.ensureFindOperation(operationQualifiedName);
    return forConsumer(referenceConfig, operationMeta, swaggerArguments);
  }

  /*
   * transport server收到请求时，创建invocation
   */
  public static Invocation forProvider(Endpoint endpoint,
      OperationMeta operationMeta,
      Object[] swaggerArguments) {
    return new Invocation(endpoint,
        operationMeta,
        swaggerArguments);
  }
}
