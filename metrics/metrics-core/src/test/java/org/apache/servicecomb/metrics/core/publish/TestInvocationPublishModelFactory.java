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
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.metrics.core.DefaultRegistryInitializer;
import org.apache.servicecomb.metrics.core.InvocationMetersInitializer;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Registry;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvocationPublishModelFactory {
  EventBus eventBus = new EventBus();

  Registry registry = new DefaultRegistry(new ManualClock());

  @Mocked
  DefaultRegistryInitializer defaultRegistryInitializer;

  InvocationMetersInitializer invocationMetersInitializer = new InvocationMetersInitializer();

  @Mocked
  Invocation invocation;

  InvocationStageTrace invocationStageTrace = new InvocationStageTrace(invocation);

  @Mocked
  Response response;

  InvocationType invocationType;

  @Test
  public void createDefaultPublishModel() throws JsonProcessingException {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getTargetService(MetricsInitializer.class, DefaultRegistryInitializer.class);
        result = defaultRegistryInitializer;
        defaultRegistryInitializer.getRegistry();
        result = registry;
      }
    };
    invocationMetersInitializer.init(null, eventBus, null);
    prepareInvocation();

    PublishModelFactory factory = new PublishModelFactory(Lists.newArrayList(registry.iterator()));
    DefaultPublishModel model = factory.createDefaultPublishModel();

    Assert.assertEquals(
        "{\"operationPerfGroups\":{\"groups\":{\"rest\":{\"200\":{\"transport\":\"rest\",\"status\":\"200\",\"operationPerfs\":[{\"operation\":\"m.s.o\",\"stages\":{\"client_filters_request\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"prepare\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_send_request\":{\"tps\":1,\"msTotalTime\":2000.0,\"msMaxLatency\":0.0},\"total\":{\"tps\":1,\"msTotalTime\":14000.0,\"msMaxLatency\":0.0},\"handlers_request\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_wait_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"client_filters_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"handlers_response\":{\"tps\":1,\"msTotalTime\":3000.0,\"msMaxLatency\":0.0},\"consumer_get_connection\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_wake_consumer\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_write_to_buf\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0}}}],\"summary\":{\"operation\":\"\",\"stages\":{\"prepare\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"client_filters_request\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"total\":{\"tps\":1,\"msTotalTime\":14000.0,\"msMaxLatency\":0.0},\"consumer_send_request\":{\"tps\":1,\"msTotalTime\":2000.0,\"msMaxLatency\":0.0},\"handlers_request\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"handlers_response\":{\"tps\":1,\"msTotalTime\":3000.0,\"msMaxLatency\":0.0},\"client_filters_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_wait_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_get_connection\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_write_to_buf\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"consumer_wake_consumer\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0}}}}}}}}",
        JsonUtils.writeValueAsString(model.getConsumer()));
    Assert.assertEquals(
        "{\"operationPerfGroups\":{\"groups\":{\"rest\":{\"200\":{\"transport\":\"rest\",\"status\":\"200\",\"operationPerfs\":[{\"operation\":\"m.s.o\",\"stages\":{\"server_filters_request\":{\"tps\":1,\"msTotalTime\":0.0,\"msMaxLatency\":0.0},\"prepare\":{\"tps\":1,\"msTotalTime\":5000.0,\"msMaxLatency\":0.0},\"execution\":{\"tps\":1,\"msTotalTime\":3000.0,\"msMaxLatency\":0.0},\"producer_send_response\":{\"tps\":1,\"msTotalTime\":2000.0,\"msMaxLatency\":0.0},\"total\":{\"tps\":1,\"msTotalTime\":14000.0,\"msMaxLatency\":0.0},\"handlers_request\":{\"tps\":1,\"msTotalTime\":6000.0,\"msMaxLatency\":0.0},\"handlers_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"queue\":{\"tps\":0,\"msTotalTime\":0.0,\"msMaxLatency\":0.0},\"server_filters_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0}}}],\"summary\":{\"operation\":\"\",\"stages\":{\"server_filters_request\":{\"tps\":1,\"msTotalTime\":0.0,\"msMaxLatency\":0.0},\"execution\":{\"tps\":1,\"msTotalTime\":3000.0,\"msMaxLatency\":0.0},\"prepare\":{\"tps\":1,\"msTotalTime\":5000.0,\"msMaxLatency\":0.0},\"total\":{\"tps\":1,\"msTotalTime\":14000.0,\"msMaxLatency\":0.0},\"producer_send_response\":{\"tps\":1,\"msTotalTime\":2000.0,\"msMaxLatency\":0.0},\"handlers_request\":{\"tps\":1,\"msTotalTime\":6000.0,\"msMaxLatency\":0.0},\"handlers_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0},\"queue\":{\"tps\":0,\"msTotalTime\":0.0,\"msMaxLatency\":0.0},\"server_filters_response\":{\"tps\":1,\"msTotalTime\":1000.0,\"msMaxLatency\":0.0}}}}}}}}",
        JsonUtils.writeValueAsString(model.getProducer()));
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
