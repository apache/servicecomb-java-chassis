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
package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.common.rest.filter.inner.RestServerCodecFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.core.invocation.ProducerInvocationFlow;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestProducerInvocationFlow extends ProducerInvocationFlow {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestProducerInvocationFlow.class);

  private static final ProduceProcessor DEFAULT_PRODUCE_PROCESSOR = ProduceProcessorManager.INSTANCE
      .findDefaultProcessor();

  public RestProducerInvocationFlow(InvocationCreator invocationCreator,
      HttpServletRequestEx requestEx, HttpServletResponseEx responseEx) {
    super(invocationCreator, requestEx, responseEx);
  }

  @Override
  protected Invocation sendCreateInvocationException(Throwable throwable) {
    try {
      Response response = Exceptions.toProducerResponse(null, throwable);
      RestServerCodecFilter.encodeResponse(null, response, DEFAULT_PRODUCE_PROCESSOR, responseEx);
    } catch (Throwable e) {
      LOGGER.error("Failed to send response when prepare invocation failed, request uri:{}",
          requestEx.getRequestURI(), e);
    }

    endResponse(null);
    return null;
  }

  @Override
  protected void endResponse(Invocation invocation, Response response) {
    invocation.getInvocationStageTrace().startProviderSendResponse();

    endResponse(invocation);
  }

  private void endResponse(Invocation invocation) {
    try {
      responseEx.endResponse();
    } catch (Throwable flushException) {
      LOGGER.error("Failed to flush rest response, operation:{}, request uri:{}",
          invocation == null ? "NA" :
              invocation.getMicroserviceQualifiedName(), requestEx.getRequestURI(), flushException);
    }

    try {
      requestEx.getAsyncContext().complete();
    } catch (Throwable completeException) {
      LOGGER.error("Failed to complete async rest response, operation:{}, request uri:{}",
          invocation == null ? "NA" :
              invocation.getMicroserviceQualifiedName(), requestEx.getRequestURI(), completeException);
    }

    if (invocation != null) {
      invocation.getInvocationStageTrace().finishProviderSendResponse();
    }
  }
}
