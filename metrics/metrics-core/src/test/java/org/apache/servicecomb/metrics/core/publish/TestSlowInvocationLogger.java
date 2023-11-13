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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Expectations;
import mockit.Mocked;

public class TestSlowInvocationLogger {
  @Mocked
  SCBEngine scbEngine;

  @Mocked
  Invocation invocation;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  RestOperationMeta restOperationMeta;

  @Mocked
  OperationConfig operationConfig;

  @Mocked
  Response response;

  @Mocked
  InvocationStageTrace stageTrace;

  InvocationFinishEvent event;

  SlowInvocationLogger logger;

  LogCollector logCollector;

  @Before
  public void setup() {
    logger = new SlowInvocationLogger(scbEngine);
    event = new InvocationFinishEvent(invocation, response);
    logCollector = new LogCollector();
  }

  @After
  public void teardown() {
    logCollector.teardown();
  }

  @Test
  public void disable() {
    logger.onInvocationFinish(event);

    Assertions.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void enableButNotSlow() {
    new Expectations() {
      {
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 2;
        stageTrace.calcTotal();
        result = 1;
      }
    };
    logger.onInvocationFinish(event);

    Assertions.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void consumerSlow(@Mocked Endpoint endpoint) {
    new Expectations() {
      {
        invocation.getEndpoint();
        result = endpoint;
        endpoint.getEndpoint();
        result = "rest://1.1.1.1:1234";
        invocation.isConsumer();
        result = true;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = restOperationMeta;
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 1;
        stageTrace.calcTotal();
        result = 1;
      }
    };
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
  public void edgeSlow(@Mocked Endpoint endpoint) {
    new Expectations() {
      {
        invocation.getEndpoint();
        result = endpoint;
        endpoint.getEndpoint();
        result = "rest://1.1.1.1:1234";
        invocation.isConsumer();
        result = true;
        invocation.isEdge();
        result = true;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = restOperationMeta;
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 1;
        stageTrace.calcTotal();
        result = 1;
      }
    };
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
  public void producerSlow(@Mocked HttpServletRequestEx requestEx) {
    new Expectations() {
      {
        invocation.getRequestEx();
        result = requestEx;
        requestEx.getRemoteAddr();
        result = "1.1.1.1";
        requestEx.getRemotePort();
        result = 1234;
        invocation.isConsumer();
        result = false;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = restOperationMeta;
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 1;
        stageTrace.calcTotal();
        result = 1;
      }
    };
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
