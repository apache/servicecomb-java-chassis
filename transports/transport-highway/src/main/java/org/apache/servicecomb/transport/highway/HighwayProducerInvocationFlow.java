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
package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.invocation.InvocationCreator;
import org.apache.servicecomb.core.invocation.ProducerInvocationFlow;
import org.apache.servicecomb.foundation.common.utils.ExceptionUtils;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighwayProducerInvocationFlow extends ProducerInvocationFlow {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayProducerInvocationFlow.class);

  private final TcpConnection connection;

  private final long msgId;

  public HighwayProducerInvocationFlow(InvocationCreator invocationCreator, TcpConnection connection, long msgId) {
    super(invocationCreator);
    this.connection = connection;
    this.msgId = msgId;
  }

  @Override
  protected Invocation sendCreateInvocationException(Throwable throwable) {
    logException(throwable);
    return null;
  }

  private void logException(Throwable throwable) {
    if (Exceptions.isPrintInvocationStackTrace()) {
      LOGGER.error("Failed to prepare invocation, msgId={}.", msgId, throwable);
      return;
    }

    LOGGER.error("Failed to prepare invocation, msgId={}, message={}.", msgId,
        ExceptionUtils.getExceptionMessageWithoutTrace(throwable));
  }

  @Override
  protected void sendResponse(Invocation invocation, Response response) {
    HighwayTransportContext transportContext = invocation.getTransportContext();
    connection.write(transportContext.getResponseBuffer().getByteBuf());
  }
}
