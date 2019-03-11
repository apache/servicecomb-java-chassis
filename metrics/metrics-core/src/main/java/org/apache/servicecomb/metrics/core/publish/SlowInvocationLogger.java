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
package org.apache.servicecomb.metrics.core.publish;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class SlowInvocationLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger(SlowInvocationLogger.class);

  public SlowInvocationLogger(SCBEngine scbEngine) {
    scbEngine.getEventBus().register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onInvocationFinish(InvocationFinishEvent event) {
    Invocation invocation = event.getInvocation();
    OperationConfig operationConfig = invocation.getOperationMeta().getConfig();
    if (!operationConfig.isSlowInvocationEnabled() ||
        invocation.getInvocationStageTrace().calcTotalTime() < operationConfig.getNanoSlowInvocation()) {
      return;
    }

    if (!invocation.isConsumer()) {
      logSlowProducer(invocation, event.getResponse(), operationConfig);
      return;
    }

    if (invocation.isEdge()) {
      logSlowEdge(invocation, event.getResponse(), operationConfig);
      return;
    }

    logSlowConsumer(invocation, event.getResponse(), operationConfig);
  }

  private String formatTime(double doubleNano) {
    long micros = TimeUnit.NANOSECONDS.toMicros((long) doubleNano);
    return micros / 1000 + "." + micros % 1000;
  }

  private void logSlowProducer(Invocation invocation, Response response, OperationConfig operationConfig) {
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();
    LOGGER.warn(invocation.getMarker(), ""
            + "slow({} ms) invocation, {}:\n"
            + "  http method: {}\n"
            + "  url        : {}\n"
            + "  status code: {}\n"
            + "  total      : {} ms\n"
            + "    prepare                : {} ms\n"
            + "    threadPoolQueue        : {} ms\n"
            + "    server filters request : {} ms\n"
            + "    handlers request       : {} ms\n"
            + "    business execute       : {} ms\n"
            + "    handlers response      : {} ms\n"
            + "    server filters response: {} ms\n"
            + "    send response          : {} ms",
        operationConfig.getMsSlowInvocation(),
        invocation.getInvocationQualifiedName(),
        restOperationMeta.getHttpMethod(),
        restOperationMeta.getAbsolutePath(),
        response.getStatusCode(),
        formatTime(stageTrace.calcTotalTime()),
        formatTime(stageTrace.calcInvocationPrepareTime()),
        formatTime(stageTrace.calcThreadPoolQueueTime()),
        formatTime(stageTrace.calcServerFiltersRequestTime()),
        formatTime(stageTrace.calcHandlersRequestTime()),
        formatTime(stageTrace.calcBusinessTime()),
        formatTime(stageTrace.calcHandlersResponseTime()),
        formatTime(stageTrace.calcServerFiltersResponseTime()),
        formatTime(stageTrace.calcSendResponseTime())
    );
  }

  private void logSlowConsumer(Invocation invocation, Response response, OperationConfig operationConfig) {
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();
    LOGGER.warn(invocation.getMarker(), ""
            + "slow({} ms) invocation, {}:\n"
            + "  http method: {}\n"
            + "  url        : {}\n"
            + "  status code: {}\n"
            + "  total      : {} ms\n"
            + "    prepare                : {} ms\n"
            + "    handlers request       : {} ms\n"
            + "    client filters request : {} ms\n"
            + "    send request           : {} ms\n"
            + "    get connection         : {} ms\n"
            + "    write to buf           : {} ms\n"
            + "    wait response          : {} ms\n"
            + "    wake consumer          : {} ms\n"
            + "    client filters response: {} ms\n"
            + "    handlers response      : {} ms",
        operationConfig.getMsSlowInvocation(),
        invocation.getInvocationQualifiedName(),
        restOperationMeta.getHttpMethod(),
        restOperationMeta.getAbsolutePath(),
        response.getStatusCode(),
        formatTime(stageTrace.calcTotalTime()),
        formatTime(stageTrace.calcInvocationPrepareTime()),
        formatTime(stageTrace.calcHandlersRequestTime()),
        formatTime(stageTrace.calcClientFiltersRequestTime()),
        formatTime(stageTrace.calcSendRequestTime()),
        formatTime(stageTrace.calcGetConnectionTime()),
        formatTime(stageTrace.calcWriteToBufferTime()),
        formatTime(stageTrace.calcReceiveResponseTime()),
        formatTime(stageTrace.calcWakeConsumer()),
        formatTime(stageTrace.calcClientFiltersResponseTime()),
        formatTime(stageTrace.calcHandlersResponseTime())
    );
  }

  private void logSlowEdge(Invocation invocation, Response response, OperationConfig operationConfig) {
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();
    LOGGER.warn(invocation.getMarker(), ""
            + "slow({} ms) invocation, {}:\n"
            + "  http method: {}\n"
            + "  url        : {}\n"
            + "  status code: {}\n"
            + "  total      : {} ms\n"
            + "    prepare                : {} ms\n"
            + "    threadPoolQueue        : {} ms\n"
            + "    server filters request : {} ms\n"
            + "    handlers request       : {} ms\n"
            + "    client filters request : {} ms\n"
            + "    send request           : {} ms\n"
            + "    get connection         : {} ms\n"
            + "    write to buf           : {} ms\n"
            + "    wait response          : {} ms\n"
            + "    wake consumer          : {} ms\n"
            + "    client filters response: {} ms\n"
            + "    handlers response      : {} ms\n"
            + "    server filters response: {} ms\n"
            + "    send response          : {} ms",
        operationConfig.getMsSlowInvocation(),
        invocation.getInvocationQualifiedName(),
        restOperationMeta.getHttpMethod(),
        restOperationMeta.getAbsolutePath(),
        response.getStatusCode(),
        formatTime(stageTrace.calcTotalTime()),
        formatTime(stageTrace.calcInvocationPrepareTime()),
        formatTime(stageTrace.calcThreadPoolQueueTime()),
        formatTime(stageTrace.calcServerFiltersRequestTime()),
        formatTime(stageTrace.calcHandlersRequestTime()),
        formatTime(stageTrace.calcClientFiltersRequestTime()),
        formatTime(stageTrace.calcSendRequestTime()),
        formatTime(stageTrace.calcGetConnectionTime()),
        formatTime(stageTrace.calcWriteToBufferTime()),
        formatTime(stageTrace.calcReceiveResponseTime()),
        formatTime(stageTrace.calcWakeConsumer()),
        formatTime(stageTrace.calcClientFiltersResponseTime()),
        formatTime(stageTrace.calcHandlersResponseTime()),
        formatTime(stageTrace.calcServerFiltersResponseTime()),
        formatTime(stageTrace.calcSendResponseTime())
    );
  }
}
