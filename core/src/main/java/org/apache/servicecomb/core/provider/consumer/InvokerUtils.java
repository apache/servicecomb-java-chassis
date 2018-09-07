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
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InvokerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvokerUtils.class);

  public static Object syncInvoke(String microserviceName, String schemaId, String operationName, Object[] args) {
    ReferenceConfig referenceConfig = SCBEngine.getInstance().getReferenceConfigForInvoke(microserviceName);
    return syncInvoke(generateInvocation(schemaId, operationName, args, referenceConfig));
  }

  public static Object syncInvoke(String microserviceName, String microserviceVersion, String transport,
      String schemaId, String operationName, Object[] args) {
    ReferenceConfig referenceConfig = SCBEngine.getInstance()
        .createReferenceConfigForInvoke(microserviceName, microserviceVersion, transport);
    return syncInvoke(generateInvocation(schemaId, operationName, args, referenceConfig));
  }

  /**
   * it's a internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   * @param invocation
   * @return contract result
   * @throws InvocationException
   */
  public static Object syncInvoke(Invocation invocation) throws InvocationException {
    Response response = innerSyncInvoke(invocation);
    if (response.isSuccessed()) {
      return response.getResult();
    }
    throw ExceptionFactory.convertConsumerException(response.getResult());
  }

  /**
   * it's a internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   * @param invocation
   * @return servicecomb response object
   */
  public static Response innerSyncInvoke(Invocation invocation) {
    try {
      invocation.onStart(null, System.nanoTime());
      SyncResponseExecutor respExecutor = new SyncResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.getInvocationStageTrace().startHandlersRequest();
      invocation.next(respExecutor::setResponse);

      Response response = respExecutor.waitResponse();
      invocation.getInvocationStageTrace().finishHandlersResponse();
      invocation.onFinish(response);
      return response;
    } catch (Throwable e) {
      String msg =
          String.format("invoke failed, %s", invocation.getOperationMeta().getMicroserviceQualifiedName());
      LOGGER.error(msg, e);

      Response response = Response.createConsumerFail(e);
      invocation.onFinish(response);
      return response;
    }
  }

  /**
   * it's a internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   * @param invocation
   * @param asyncResp
   */
  public static void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
    try {
      invocation.onStart(null, System.nanoTime());
      invocation.setSync(false);

      ReactiveResponseExecutor respExecutor = new ReactiveResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.getInvocationStageTrace().startHandlersRequest();
      invocation.next(ar -> {
        ContextUtils.setInvocationContext(invocation.getParentContext());
        try {
          invocation.getInvocationStageTrace().finishHandlersResponse();
          invocation.onFinish(ar);
          asyncResp.handle(ar);
        } finally {
          ContextUtils.removeInvocationContext();
        }
      });
    } catch (Throwable e) {
      invocation.getInvocationStageTrace().finishHandlersResponse();
      //if throw exception,we can use 500 for status code ?
      Response response = Response.createConsumerFail(e);
      invocation.onFinish(response);
      LOGGER.error("invoke failed, {}", invocation.getOperationMeta().getMicroserviceQualifiedName());
      asyncResp.handle(response);
    }
  }

  private static Invocation generateInvocation(String schemaId, String operationName, Object[] args,
      ReferenceConfig referenceConfig) {
    SchemaMeta schemaMeta = referenceConfig.getMicroserviceMeta().ensureFindSchemaMeta(schemaId);
    return InvocationFactory.forConsumer(referenceConfig, schemaMeta, operationName, args);
  }

  @Deprecated
  public static Object invoke(Invocation invocation) {
    return syncInvoke(invocation);
  }
}
