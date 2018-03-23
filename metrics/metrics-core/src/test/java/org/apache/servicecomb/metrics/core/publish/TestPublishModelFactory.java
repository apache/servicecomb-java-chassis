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

import java.util.List;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.metrics.core.DefaultMetricsInitializer;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.ManualClock;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.SpectatorUtils;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestPublishModelFactory {
  @Mocked
  Invocation invocation;

  @Mocked
  Response response;

  InvocationType invocationType;

  @Test
  public void createDefaultPublishModel() throws JsonProcessingException {
    Registry registry = prepareRegistry();
    List<Meter> meters = Lists.newArrayList(registry);
    PublishModelFactory factory = new PublishModelFactory(meters);
    DefaultPublishModel model = factory.createDefaultPublishModel();

    Assert.assertEquals(
        "{\"consumer\":{\"operationPerfGroups\":{\"groups\":{\"rest\":{\"200\":{\"transport\":\"rest\",\"status\":\"200\",\"operationPerfs\":[{\"operation\":\"m.s.o\",\"stages\":{\"total\":{\"tps\":1,\"msTotalTime\":10000.0,\"msMaxLatency\":0.0}}}],\"summary\":{\"operation\":\"\",\"stages\":{\"total\":{\"tps\":1,\"msTotalTime\":10000.0,\"msMaxLatency\":0.0}}}}}}}},\"producer\":{\"operationPerfGroups\":{\"groups\":{\"rest\":{\"200\":{\"transport\":\"rest\",\"status\":\"200\",\"operationPerfs\":[{\"operation\":\"m.s.o\",\"stages\":{\"execution\":{\"tps\":1,\"msTotalTime\":5000.0,\"msMaxLatency\":0.0},\"total\":{\"tps\":1,\"msTotalTime\":10000.0,\"msMaxLatency\":0.0},\"queue\":{\"tps\":1,\"msTotalTime\":5000.0,\"msMaxLatency\":0.0}}}],\"summary\":{\"operation\":\"\",\"stages\":{\"execution\":{\"tps\":1,\"msTotalTime\":5000.0,\"msMaxLatency\":0.0},\"total\":{\"tps\":1,\"msTotalTime\":10000.0,\"msMaxLatency\":0.0},\"queue\":{\"tps\":1,\"msTotalTime\":5000.0,\"msMaxLatency\":0.0}}}}}}}}}",
        JsonUtils.writeValueAsString(model));
  }

  protected Registry prepareRegistry() {
    CompositeRegistry globalRegistry = SpectatorUtils.createCompositeRegistry(null);
    Registry registry = new DefaultRegistry(new ManualClock());
    EventBus eventBus = new EventBus();

    DefaultMetricsInitializer metricsInitializer = new DefaultMetricsInitializer() {
      protected Registry createRegistry(MetricsBootstrapConfig config) {
        return registry;
      };
    };
    metricsInitializer.init(globalRegistry, eventBus, new MetricsBootstrapConfig());

    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return 10;
      }
    };

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
      long getStartExecutionTime() {
        return 5;
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

    return registry;
  }
}
