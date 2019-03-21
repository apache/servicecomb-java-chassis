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

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.metrics.core.InvocationMetersInitializer;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;

import io.vertx.core.json.Json;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvocationPublishModelFactory {
  EventBus eventBus = new EventBus();

  GlobalRegistry globalRegistry = new GlobalRegistry();

  Registry registry = new DefaultRegistry(new ManualClock());

  InvocationMetersInitializer invocationMetersInitializer = new InvocationMetersInitializer();

  @Mocked
  Invocation invocation;

  InvocationStageTrace invocationStageTrace = new InvocationStageTrace(invocation);

  @Mocked
  Response response;

  InvocationType invocationType;

  @Test
  public void createDefaultPublishModel() {
    ArchaiusUtils.setProperty("servicecomb.metrics.invocation.latencyDistribution", "0,1,100");
    globalRegistry.add(registry);
    invocationMetersInitializer.init(globalRegistry, eventBus, null);
    prepareInvocation();

    globalRegistry.poll(1);
    PublishModelFactory factory = new PublishModelFactory(Lists.newArrayList(registry.iterator()));
    DefaultPublishModel model = factory.createDefaultPublishModel();

    String expect = "{\n"
        + "  \"operationPerfGroups\" : {\n"
        + "    \"groups\" : {\n"
        + "      \"rest\" : {\n"
        + "        \"200\" : {\n"
        + "          \"transport\" : \"rest\",\n"
        + "          \"status\" : \"200\",\n"
        + "          \"operationPerfs\" : [ {\n"
        + "            \"operation\" : \"m.s.o\",\n"
        + "            \"stages\" : {\n"
        + "              \"prepare\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"client_filters_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"total\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.4000000000000001E-5,\n"
        + "                \"msMaxLatency\" : 1.4000000000000001E-5\n"
        + "              },\n"
        + "              \"consumer_send_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 2.0000000000000003E-6,\n"
        + "                \"msMaxLatency\" : 2.0000000000000003E-6\n"
        + "              },\n"
        + "              \"handlers_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"handlers_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 3.0000000000000005E-6,\n"
        + "                \"msMaxLatency\" : 3.0000000000000005E-6\n"
        + "              },\n"
        + "              \"consumer_wait_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"client_filters_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_get_connection\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_write_to_buf\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_wake_consumer\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              }\n"
        + "            },\n"
        + "            \"latencyDistribution\" : [ 1, 0, 0 ]\n"
        + "          } ],\n"
        + "          \"summary\" : {\n"
        + "            \"operation\" : \"\",\n"
        + "            \"stages\" : {\n"
        + "              \"client_filters_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"prepare\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_send_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 2.0000000000000003E-6,\n"
        + "                \"msMaxLatency\" : 2.0000000000000003E-6\n"
        + "              },\n"
        + "              \"total\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.4000000000000001E-5,\n"
        + "                \"msMaxLatency\" : 1.4000000000000001E-5\n"
        + "              },\n"
        + "              \"handlers_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"client_filters_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_wait_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"handlers_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 3.0000000000000005E-6,\n"
        + "                \"msMaxLatency\" : 3.0000000000000005E-6\n"
        + "              },\n"
        + "              \"consumer_get_connection\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_wake_consumer\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"consumer_write_to_buf\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              }\n"
        + "            },\n"
        + "            \"latencyDistribution\" : [ 1, 0, 0 ]\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + "}";
    Assert.assertEquals(Json.encodePrettily(Json.decodeValue(expect, Object.class)),
        Json.encodePrettily(model.getConsumer()));

    expect = "{\n"
        + "  \"operationPerfGroups\" : {\n"
        + "    \"groups\" : {\n"
        + "      \"rest\" : {\n"
        + "        \"200\" : {\n"
        + "          \"transport\" : \"rest\",\n"
        + "          \"status\" : \"200\",\n"
        + "          \"operationPerfs\" : [ {\n"
        + "            \"operation\" : \"m.s.o\",\n"
        + "            \"stages\" : {\n"
        + "              \"server_filters_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 0.0,\n"
        + "                \"msMaxLatency\" : 0.0\n"
        + "              },\n"
        + "              \"prepare\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 5.0E-6,\n"
        + "                \"msMaxLatency\" : 5.0E-6\n"
        + "              },\n"
        + "              \"execution\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 3.0000000000000005E-6,\n"
        + "                \"msMaxLatency\" : 3.0000000000000005E-6\n"
        + "              },\n"
        + "              \"total\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.4000000000000001E-5,\n"
        + "                \"msMaxLatency\" : 1.4000000000000001E-5\n"
        + "              },\n"
        + "              \"producer_send_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 2.0000000000000003E-6,\n"
        + "                \"msMaxLatency\" : 2.0000000000000003E-6\n"
        + "              },\n"
        + "              \"handlers_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 6.000000000000001E-6,\n"
        + "                \"msMaxLatency\" : 6.000000000000001E-6\n"
        + "              },\n"
        + "              \"handlers_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"queue\" : {\n"
        + "                \"tps\" : 0.0,\n"
        + "                \"msTotalTime\" : 0.0,\n"
        + "                \"msMaxLatency\" : 0.0\n"
        + "              },\n"
        + "              \"server_filters_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              }\n"
        + "            },\n"
        + "            \"latencyDistribution\" : [ 1, 0, 0 ]\n"
        + "          } ],\n"
        + "          \"summary\" : {\n"
        + "            \"operation\" : \"\",\n"
        + "            \"stages\" : {\n"
        + "              \"server_filters_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 0.0,\n"
        + "                \"msMaxLatency\" : 0.0\n"
        + "              },\n"
        + "              \"execution\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 3.0000000000000005E-6,\n"
        + "                \"msMaxLatency\" : 3.0000000000000005E-6\n"
        + "              },\n"
        + "              \"prepare\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 5.0E-6,\n"
        + "                \"msMaxLatency\" : 5.0E-6\n"
        + "              },\n"
        + "              \"producer_send_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 2.0000000000000003E-6,\n"
        + "                \"msMaxLatency\" : 2.0000000000000003E-6\n"
        + "              },\n"
        + "              \"total\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.4000000000000001E-5,\n"
        + "                \"msMaxLatency\" : 1.4000000000000001E-5\n"
        + "              },\n"
        + "              \"handlers_request\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 6.000000000000001E-6,\n"
        + "                \"msMaxLatency\" : 6.000000000000001E-6\n"
        + "              },\n"
        + "              \"handlers_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              },\n"
        + "              \"queue\" : {\n"
        + "                \"tps\" : 0.0,\n"
        + "                \"msTotalTime\" : 0.0,\n"
        + "                \"msMaxLatency\" : 0.0\n"
        + "              },\n"
        + "              \"server_filters_response\" : {\n"
        + "                \"tps\" : 1.0,\n"
        + "                \"msTotalTime\" : 1.0000000000000002E-6,\n"
        + "                \"msMaxLatency\" : 1.0000000000000002E-6\n"
        + "              }\n"
        + "            },\n"
        + "            \"latencyDistribution\" : [ 1, 0, 0 ]\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + "}";
    Assert.assertEquals(Json.encodePrettily(Json.decodeValue(expect, Object.class)),
        Json.encodePrettily(model.getProducer()));
  }

  protected void prepareInvocation() {

    Deencapsulation.setField(invocationStageTrace, "start", 1L);
    Deencapsulation.setField(invocationStageTrace, "startHandlersRequest", 2L);
    Deencapsulation.setField(invocationStageTrace, "startClientFiltersRequest", 3L);
    Deencapsulation.setField(invocationStageTrace, "startSend", 4L);
    Deencapsulation.setField(invocationStageTrace, "finishGetConnection", 5L);
    Deencapsulation.setField(invocationStageTrace, "finishWriteToBuffer", 6L);
    Deencapsulation.setField(invocationStageTrace, "finishReceiveResponse", 7L);
    Deencapsulation.setField(invocationStageTrace, "startClientFiltersResponse", 8L);
    Deencapsulation.setField(invocationStageTrace, "finishClientFiltersResponse", 9L);
    Deencapsulation.setField(invocationStageTrace, "finishHandlersResponse", 14L);
    Deencapsulation.setField(invocationStageTrace, "finish", 15L);
    Deencapsulation.setField(invocationStageTrace, "startExecution", 5L);
    Deencapsulation.setField(invocationStageTrace, "startSchedule", 6L);
    Deencapsulation.setField(invocationStageTrace, "startBusinessMethod", 8L);
    Deencapsulation.setField(invocationStageTrace, "finishBusiness", 11L);
    Deencapsulation.setField(invocationStageTrace, "finishHandlersResponse", 12L);
    Deencapsulation.setField(invocationStageTrace, "finishServerFiltersResponse", 13L);
    Deencapsulation.setField(invocationStageTrace, "invocation", invocation);

    invocationType = InvocationType.CONSUMER;
    new MockUp<Invocation>() {
      @Mock
      InvocationType getInvocationType() {
        return invocationType;
      }

      @Mock
      boolean isConsumer() {
        return InvocationType.CONSUMER.equals(invocationType);
      }

      @Mock
      String getRealTransportName() {
        return Const.RESTFUL;
      }

      @Mock
      String getMicroserviceQualifiedName() {
        return "m.s.o";
      }

      @Mock
      InvocationStageTrace getInvocationStageTrace() {
        return invocationStageTrace;
      }
    };

    new Expectations() {
      {
        response.getStatusCode();
        result = 200;
      }
    };
    InvocationFinishEvent finishEvent = new InvocationFinishEvent(invocation, response);
    eventBus.post(finishEvent);

    invocationType = InvocationType.PRODUCER;
    eventBus.post(finishEvent);
  }
}
