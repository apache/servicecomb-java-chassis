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

import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_CONSUMER_CONNECTION;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_CONSUMER_DECODE_RESPONSE;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_CONSUMER_ENCODE_REQUEST;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_CONSUMER_SEND;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_CONSUMER_WAIT;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_PREPARE;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_PROVIDER_BUSINESS;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_PROVIDER_DECODE_REQUEST;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_PROVIDER_ENCODE_RESPONSE;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_PROVIDER_QUEUE;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_PROVIDER_SEND;
import static org.apache.servicecomb.core.invocation.InvocationStageTrace.STAGE_TOTAL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class SlowInvocationLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger("scb-slow");

  public SlowInvocationLogger(SCBEngine scbEngine) {
    scbEngine.getEventBus().register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onInvocationFinish(InvocationFinishEvent event) {
    Invocation invocation = event.getInvocation();
    OperationConfig operationConfig = invocation.getOperationMeta().getConfig();
    if (!operationConfig.isSlowInvocationEnabled() ||
        invocation.getInvocationStageTrace().calcTotal() < operationConfig.getNanoSlowInvocation()) {
      return;
    }

    if (invocation.isProducer()) {
      logSlowProvider(invocation, event.getResponse(), operationConfig);
      return;
    }

    if (invocation.isEdge()) {
      logSlowEdge(invocation, event.getResponse(), operationConfig);
      return;
    }

    logSlowConsumer(invocation, event.getResponse(), operationConfig);
  }

  private static String collectClientAddress(Invocation invocation) {
    HttpServletRequestEx requestEx = invocation.getRequestEx();
    return requestEx == null ? "unknown" : requestEx.getRemoteAddr() + ":" + requestEx.getRemotePort();
  }

  private static String collectTargetAddress(Invocation invocation) {
    Endpoint endpoint = invocation.getEndpoint();
    return endpoint == null ? "unknown" : endpoint.getEndpoint();
  }

  private static String formatTime(long doubleNano) {
    long micros = TimeUnit.NANOSECONDS.toMicros(doubleNano);
    return micros / 1000 + "." + micros % 1000;
  }

  private static void logSlowProvider(Invocation invocation, Response response, OperationConfig operationConfig) {
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();
    StringBuilder sb = new StringBuilder();
    sb.append("Slow Provider invocation [").append(invocation.getInvocationQualifiedName())
        .append("](").append(operationConfig.getMsSlowInvocation()).append(" ms")
        .append(")[").append(invocation.getTraceId()).append("]\n")
        .append(formatPair("  ", "http method", restOperationMeta.getHttpMethod()))
        .append(formatPair("  ", "url", restOperationMeta.getAbsolutePath()))
        .append(formatPair("  ", "endpoint", collectClientAddress(invocation)))
        .append(formatPair("  ", "status code", String.valueOf(response.getStatusCode())))

        .append(formatPair("    ", STAGE_TOTAL, stageTrace.calcTotal()))
        .append(formatPair("    ", STAGE_PREPARE, stageTrace.calcPrepare()))
        .append(formatPair("    ", STAGE_PROVIDER_DECODE_REQUEST, stageTrace.calcProviderDecodeRequest()))
        .append(formatPair("    ", STAGE_PROVIDER_QUEUE, stageTrace.calcQueue()))
        .append(formatPair("    ", STAGE_PROVIDER_BUSINESS, stageTrace.calcBusinessExecute()))
        .append(formatPair("    ", STAGE_PROVIDER_ENCODE_RESPONSE, stageTrace.calcProviderEncodeResponse()))
        .append(formatPair("    ", STAGE_PROVIDER_SEND, stageTrace.calcProviderSendResponse()));

    List<String> sorted = new ArrayList<>(stageTrace.getStages().keySet());
    sorted.stream().sorted().forEach(key -> {
      sb.append(formatPair("    ", key,
          InvocationStageTrace.calc(stageTrace.getStages().get(key).getEndTime(),
              stageTrace.getStages().get(key).getBeginTime())));
    });

    LOGGER.warn(sb.toString());
  }

  protected static String formatPair(String padding, String name, String value) {
    return String.format("%-20s: %20s\n", padding + name, value);
  }

  protected static String formatPair(String padding, String name, long time) {
    return String.format("%-20s: %8sms\n", padding + name, formatTime(time));
  }

  private static void logSlowConsumer(Invocation invocation, Response response, OperationConfig operationConfig) {
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();
    StringBuilder sb = new StringBuilder();
    sb.append("Slow Consumer invocation [").append(invocation.getInvocationQualifiedName())
        .append("](").append(operationConfig.getMsSlowInvocation()).append(" ms")
        .append(")[").append(invocation.getTraceId()).append("]\n")
        .append(formatPair("  ", "http method", restOperationMeta.getHttpMethod()))
        .append(formatPair("  ", "url", restOperationMeta.getAbsolutePath()))
        .append(formatPair("  ", "endpoint", collectTargetAddress(invocation)))
        .append(formatPair("  ", "status code", String.valueOf(response.getStatusCode())))
        .append(formatPair("    ", STAGE_TOTAL, stageTrace.calcTotal()))
        .append(formatPair("    ", STAGE_PREPARE, stageTrace.calcPrepare()))
        .append(formatPair("    ", STAGE_CONSUMER_CONNECTION, stageTrace.calcConnection()))
        .append(formatPair("    ", STAGE_CONSUMER_ENCODE_REQUEST, stageTrace.calcConsumerEncodeRequest()))
        .append(formatPair("    ", STAGE_CONSUMER_SEND, stageTrace.calcConsumerSendRequest()))
        .append(formatPair("    ", STAGE_CONSUMER_WAIT, stageTrace.calcWait()))
        .append(formatPair("    ", STAGE_CONSUMER_DECODE_RESPONSE, stageTrace.calcConsumerDecodeResponse()));

    List<String> sorted = new ArrayList<>(stageTrace.getStages().keySet());
    sorted.stream().sorted().forEach(key -> {
      sb.append(formatPair("    ", key,
          InvocationStageTrace.calc(stageTrace.getStages().get(key).getEndTime(),
              stageTrace.getStages().get(key).getBeginTime())));
    });

    LOGGER.warn(sb.toString());
  }

  private static void logSlowEdge(Invocation invocation, Response response, OperationConfig operationConfig) {
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = invocation.getInvocationStageTrace();
    StringBuilder sb = new StringBuilder();
    sb.append("Slow Edge invocation [").append(invocation.getInvocationQualifiedName())
        .append("](").append(operationConfig.getMsSlowInvocation()).append(" ms")
        .append(")[").append(invocation.getTraceId()).append("]\n")
        .append(formatPair("  ", "http method", restOperationMeta.getHttpMethod()))
        .append(formatPair("  ", "url", restOperationMeta.getAbsolutePath()))
        .append(formatPair("  ", "endpoint", collectTargetAddress(invocation)))
        .append(formatPair("  ", "status code", String.valueOf(response.getStatusCode())))
        .append(formatPair("    ", STAGE_TOTAL, stageTrace.calcTotal()))
        .append(formatPair("    ", STAGE_PREPARE, stageTrace.calcPrepare()))
        .append(formatPair("    ", STAGE_PROVIDER_DECODE_REQUEST, stageTrace.calcProviderDecodeRequest()))
        .append(formatPair("    ", STAGE_CONSUMER_CONNECTION, stageTrace.calcConnection()))
        .append(formatPair("    ", STAGE_CONSUMER_ENCODE_REQUEST, stageTrace.calcConsumerEncodeRequest()))
        .append(formatPair("    ", STAGE_CONSUMER_SEND, stageTrace.calcConsumerSendRequest()))
        .append(formatPair("    ", STAGE_CONSUMER_WAIT, stageTrace.calcWait()))
        .append(formatPair("    ", STAGE_CONSUMER_DECODE_RESPONSE, stageTrace.calcConsumerDecodeResponse()))
        .append(formatPair("    ", STAGE_PROVIDER_ENCODE_RESPONSE, stageTrace.calcProviderEncodeResponse()))
        .append(formatPair("    ", STAGE_PROVIDER_SEND, stageTrace.calcProviderSendResponse()))
    ;

    List<String> sorted = new ArrayList<>(stageTrace.getStages().keySet());
    sorted.stream().sorted().forEach(key -> {
      sb.append(formatPair("    ", key,
          InvocationStageTrace.calc(stageTrace.getStages().get(key).getEndTime(),
              stageTrace.getStages().get(key).getBeginTime())));
    });

    LOGGER.warn(sb.toString());
  }
}
