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
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.OperationConfig;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    ArchaiusUtils.resetConfig();
    logCollector = new LogCollector();
  }

  @After
  public void teardown() {
    logCollector.teardown();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void disable() {
    logger.onInvocationFinish(event);

    Assert.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void enableButNotSlow() {
    new Expectations() {
      {
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 2;
        stageTrace.calcTotalTime();
        result = 1;
      }
    };
    logger.onInvocationFinish(event);

    Assert.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void consumerSlow() {
    new Expectations() {
      {
        invocation.isConsumer();
        result = true;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = restOperationMeta;
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 1;
        stageTrace.calcTotalTime();
        result = 1;
      }
    };
    logger.onInvocationFinish(event);

    Assert.assertEquals(""
            + "slow(0 ms) invocation, null:\n"
            + "  http method: null\n"
            + "  url        : null\n"
            + "  status code: 0\n"
            + "  traceId    : null\n"
            + "  total      : 0.0 ms\n"
            + "    prepare                : 0.0 ms\n"
            + "    handlers request       : 0.0 ms\n"
            + "    client filters request : 0.0 ms\n"
            + "    send request           : 0.0 ms\n"
            + "    get connection         : 0.0 ms\n"
            + "    write to buf           : 0.0 ms\n"
            + "    wait response          : 0.0 ms\n"
            + "    wake consumer          : 0.0 ms\n"
            + "    client filters response: 0.0 ms\n"
            + "    handlers response      : 0.0 ms",
        logCollector.getEvents().get(0).getMessage());
  }

  @Test
  public void edgeSlow() {
    new Expectations() {
      {
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
        stageTrace.calcTotalTime();
        result = 1;
      }
    };
    logger.onInvocationFinish(event);

    Assert.assertEquals(""
            + "slow(0 ms) invocation, null:\n"
            + "  http method: null\n"
            + "  url        : null\n"
            + "  status code: 0\n"
            + "  traceId    : null\n"
            + "  total      : 0.0 ms\n"
            + "    prepare                : 0.0 ms\n"
            + "    threadPoolQueue        : 0.0 ms\n"
            + "    server filters request : 0.0 ms\n"
            + "    handlers request       : 0.0 ms\n"
            + "    client filters request : 0.0 ms\n"
            + "    send request           : 0.0 ms\n"
            + "    get connection         : 0.0 ms\n"
            + "    write to buf           : 0.0 ms\n"
            + "    wait response          : 0.0 ms\n"
            + "    wake consumer          : 0.0 ms\n"
            + "    client filters response: 0.0 ms\n"
            + "    handlers response      : 0.0 ms\n"
            + "    server filters response: 0.0 ms\n"
            + "    send response          : 0.0 ms",
        logCollector.getEvents().get(0).getMessage());
  }

  @Test
  public void producerSlow() {
    new Expectations() {
      {
        invocation.isConsumer();
        result = false;
        operationMeta.getExtData(RestConst.SWAGGER_REST_OPERATION);
        result = restOperationMeta;
        operationConfig.isSlowInvocationEnabled();
        result = true;
        operationConfig.getNanoSlowInvocation();
        result = 1;
        stageTrace.calcTotalTime();
        result = 1;
      }
    };
    logger.onInvocationFinish(event);

    Assert.assertEquals(""
            + "slow(0 ms) invocation, null:\n"
            + "  http method: null\n"
            + "  url        : null\n"
            + "  status code: 0\n"
            + "  traceId    : null\n"
            + "  total      : 0.0 ms\n"
            + "    prepare                : 0.0 ms\n"
            + "    threadPoolQueue        : 0.0 ms\n"
            + "    server filters request : 0.0 ms\n"
            + "    server filters request : 0.0 ms\n"
            + "    handlers request       : 0.0 ms\n"
            + "    business execute       : 0.0 ms\n"
            + "    handlers response      : 0.0 ms\n"
            + "    server filters response: 0.0 ms\n"
            + "    send response          : {} ms",
        logCollector.getEvents().get(0).getMessage());
  }
}
