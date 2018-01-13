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

package org.apache.servicecomb.core.provider.consumer;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.metrics.InvocationStartedEvent;
import org.apache.servicecomb.foundation.common.utils.EventUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      triggerStartedEvent(invocation);
      SyncResponseExecutor respExecutor = new SyncResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.next(respExecutor::setResponse);

      return respExecutor.waitResponse();
    } catch (Throwable e) {
      String msg =
          String.format("invoke failed, %s", invocation.getOperationMeta().getMicroserviceQualifiedName());
      LOGGER.debug(msg, e);
      return Response.createConsumerFail(e);
    } finally {
      invocation.triggerFinishedEvent();
    }
  }

  public static void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
    try {
      triggerStartedEvent(invocation);
      invocation.setSync(false);

      ReactiveResponseExecutor respExecutor = new ReactiveResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.next(ar -> {
        invocation.triggerFinishedEvent();
        asyncResp.handle(ar);
      });

    } catch (Throwable e) {
      invocation.triggerFinishedEvent();
      LOGGER.error("invoke failed, {}", invocation.getOperationMeta().getMicroserviceQualifiedName());
      asyncResp.consumerFail(e);
    }
  }

  @Deprecated
  public static Object invoke(Invocation invocation) {
    return syncInvoke(invocation);
  }

  private static void triggerStartedEvent(Invocation invocation) {
    long startTime = System.nanoTime();
    EventUtils.triggerEvent(new InvocationStartedEvent(invocation.getMicroserviceQualifiedName(),
        InvocationType.CONSUMER, startTime));
    invocation.setStartTime(startTime);
  }
}
