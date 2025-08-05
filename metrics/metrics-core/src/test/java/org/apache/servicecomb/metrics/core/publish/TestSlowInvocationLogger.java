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

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.eventbus.EventBus;

@ExtendWith(MockitoExtension.class)
public class TestSlowInvocationLogger {
  @Mock
  SCBEngine scbEngine;

  @Mock
  Invocation invocation;

  @Mock
  OperationMeta operationMeta;

  @Mock
  RestOperationMeta restOperationMeta;

  @Mock
  OperationConfig operationConfig;

  @Mock
  Response response;

  @Mock
  InvocationStageTrace stageTrace;

  InvocationFinishEvent event;

  SlowInvocationLogger logger;

  LogCollector logCollector;

  @BeforeEach
  public void setup() {
    EventBus eventBus = Mockito.spy(EventBus.class);
    Mockito.doNothing().when(eventBus).register(Mockito.any());
    Mockito.when(scbEngine.getEventBus()).thenReturn(eventBus);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getConfig()).thenReturn(operationConfig);

    logger = new SlowInvocationLogger(scbEngine);
    event = new InvocationFinishEvent(invocation, response);
    logCollector = new LogCollector();
  }

  @AfterEach
  public void tearDown() {
    logCollector.tearDown();
  }

  @Test
  public void disable() {
    logger.onInvocationFinish(event);

    Assertions.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void enableButNotSlow() {

    Mockito.when(operationConfig.isSlowInvocationEnabled()).thenReturn(true);
    Mockito.when(operationConfig.getNanoSlowInvocation()).thenReturn(2L);
    Mockito.when(stageTrace.calcTotal()).thenReturn(1L);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(stageTrace);
    logger.onInvocationFinish(event);

    Assertions.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void consumerSlow() {
    Endpoint endpoint = Mockito.mock(Endpoint.class);
    Mockito.when(invocation.getEndpoint()).thenReturn(endpoint);
    Mockito.when(endpoint.getEndpoint()).thenReturn("rest://1.1.1.1:1234");
    Mockito.when(invocation.isProducer()).thenReturn(false);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(restOperationMeta);
    Mockito.when(operationConfig.isSlowInvocationEnabled()).thenReturn(true);
    Mockito.when(operationConfig.getNanoSlowInvocation()).thenReturn(1L);
    Mockito.when(stageTrace.calcTotal()).thenReturn(1L);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(stageTrace);
    logger.onInvocationFinish(event);

    Assertions.assertEquals("""
            Slow Consumer invocation [null](0 ms)[null]
              http method       :                 null
              url               :                 null
              endpoint          :  rest://1.1.1.1:1234
              status code       :                    0
                total           :      0.0ms
                prepare         :      0.0ms
                connection      :      0.0ms
                consumer-encode :      0.0ms
                consumer-send   :      0.0ms
                wait            :      0.0ms
                consumer-decode :      0.0ms
            """,
        logCollector.getEvent(0).getMessage().getFormattedMessage());
  }

  @Test
  public void edgeSlow() {
    Endpoint endpoint = Mockito.mock(Endpoint.class);
    Mockito.when(invocation.getEndpoint()).thenReturn(endpoint);
    Mockito.when(endpoint.getEndpoint()).thenReturn("rest://1.1.1.1:1234");
    Mockito.when(invocation.isEdge()).thenReturn(true);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(restOperationMeta);
    Mockito.when(operationConfig.isSlowInvocationEnabled()).thenReturn(true);
    Mockito.when(operationConfig.getNanoSlowInvocation()).thenReturn(1L);
    Mockito.when(stageTrace.calcTotal()).thenReturn(1L);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(stageTrace);
    logger.onInvocationFinish(event);

    Assertions.assertEquals("""
            Slow Edge invocation [null](0 ms)[null]
              http method       :                 null
              url               :                 null
              endpoint          :  rest://1.1.1.1:1234
              status code       :                    0
                total           :      0.0ms
                prepare         :      0.0ms
                provider-decode :      0.0ms
                connection      :      0.0ms
                consumer-encode :      0.0ms
                consumer-send   :      0.0ms
                wait            :      0.0ms
                consumer-decode :      0.0ms
                provider-encode :      0.0ms
                provider-send   :      0.0ms
            """,
        logCollector.getEvent(0).getMessage().getFormattedMessage());
  }

  @Test
  public void producerSlow() {

    HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

    Mockito.when(invocation.getRequestEx()).thenReturn(requestEx);
    Mockito.when(requestEx.getRemoteAddr()).thenReturn("1.1.1.1");
    Mockito.when(requestEx.getRemotePort()).thenReturn(1234);
    Mockito.when(invocation.isProducer()).thenReturn(true);
    Mockito.when(operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION)).thenReturn(restOperationMeta);
    Mockito.when(operationConfig.isSlowInvocationEnabled()).thenReturn(true);
    Mockito.when(operationConfig.getNanoSlowInvocation()).thenReturn(1L);
    Mockito.when(stageTrace.calcTotal()).thenReturn(1L);
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(stageTrace);

    logger.onInvocationFinish(event);

    Assertions.assertEquals("""
            Slow Provider invocation [null](0 ms)[null]
              http method       :                 null
              url               :                 null
              endpoint          :         1.1.1.1:1234
              status code       :                    0
                total           :      0.0ms
                prepare         :      0.0ms
                provider-decode :      0.0ms
                queue           :      0.0ms
                execute         :      0.0ms
                provider-encode :      0.0ms
                provider-send   :      0.0ms
            """,
        logCollector.getEvent(0).getMessage().getFormattedMessage());
  }
}
