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

package io.servicecomb.core.provider.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.swagger.invocation.AsyncResponse;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;
import io.servicecomb.swagger.invocation.exception.InvocationException;

public final class InvokerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvokerUtils.class);

  private InvokerUtils() {
  }

  public static Object syncInvoke(String microserviceName, String schemaId, String operationName, Object[] args) {
    ReferenceConfig referenceConfig = ReferenceConfigUtils.getForInvoke(microserviceName);
    SchemaMeta schemaMeta = referenceConfig.getMicroserviceMeta().ensureFindSchemaMeta(schemaId);
    Invocation invocation = InvocationFactory.forConsumer(referenceConfig, schemaMeta, operationName, args);
    return syncInvoke(invocation);
  }

  public static Object syncInvoke(String microserviceName, String microserviceVersion, String transport,
      String schemaId, String operationName, Object[] args) {
    ReferenceConfig referenceConfig =
        ReferenceConfigUtils.getForInvoke(microserviceName, microserviceVersion, transport);
    SchemaMeta schemaMeta = referenceConfig.getMicroserviceMeta().ensureFindSchemaMeta(schemaId);
    Invocation invocation = InvocationFactory.forConsumer(referenceConfig, schemaMeta, operationName, args);
    return syncInvoke(invocation);
  }

  public static Object syncInvoke(Invocation invocation) throws InvocationException {
    Response response = innerSyncInvoke(invocation);
    if (response.isSuccessed()) {
      return response.getResult();
    }

    throw ExceptionFactory.convertConsumerException((Throwable) response.getResult());
  }

  public static Response innerSyncInvoke(Invocation invocation) {
    try {
      SyncResponseExecutor respExecutor = new SyncResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.next(resp -> {
        respExecutor.setResponse(resp);
      });

      return respExecutor.waitResponse();
    } catch (Throwable e) {
      String msg =
          String.format("invoke failed, %s", invocation.getOperationMeta().getMicroserviceQualifiedName());
      LOGGER.debug(msg, e);
      return Response.createConsumerFail(e);
    }
  }

  public static void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
    try {
      ReactiveResponseExecutor respExecutor = new ReactiveResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.next(asyncResp);
    } catch (Throwable e) {
      LOGGER.error("invoke failed, {}", invocation.getOperationMeta().getMicroserviceQualifiedName());
      asyncResp.consumerFail(e);
    }
  }

  public static Object invoke(Invocation invocation) {
    if (invocation.getOperationMeta().isSync()) {
      return syncInvoke(invocation);
    }

    Object[] args = invocation.getArgs();
    AsyncResponse asyncResp = (AsyncResponse) args[args.length - 1];
    reactiveInvoke(invocation, asyncResp);
    return null;
  }
}
