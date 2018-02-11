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

package org.apache.servicecomb.metrics.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.metrics.health.HealthCheckResult;
import org.apache.servicecomb.foundation.metrics.health.HealthChecker;
import org.apache.servicecomb.foundation.metrics.health.HealthCheckerManager;
import org.apache.servicecomb.metrics.core.health.DefaultHealthCheckExtraData;
import org.apache.servicecomb.metrics.core.health.DefaultMicroserviceHealthChecker;
import org.apache.servicecomb.metrics.core.publish.DefaultHealthCheckerPublisher;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;

public class TestHealthCheckerPublisher {

  @Test
  public void testPublisher() throws IOException {
    Microservice microservice = new Microservice();
    microservice.setAppId("appId");
    microservice.setServiceName("serviceName");
    microservice.setVersion("0.0.1");

    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setEndpoints(Lists.newArrayList("127.0.0.1", "192.168.0.100"));
    microserviceInstance.setInstanceId("001");
    microserviceInstance.setHostName("localhost");

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
      }
    };

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };

    HealthCheckerManager manager = mock(HealthCheckerManager.class);

    Map<String, HealthCheckResult> results = new HashMap<>();
    HealthChecker result = new DefaultMicroserviceHealthChecker();
    results.put("default", result.check());

    when(manager.check()).thenReturn(results);
    when(manager.check("default")).thenReturn(result.check());

    DefaultHealthCheckerPublisher publisher = new DefaultHealthCheckerPublisher(manager);
    Map<String, HealthCheckResult> content = publisher.health();

    DefaultHealthCheckExtraData data = JsonUtils.OBJ_MAPPER
        .readValue(content.get("default").getExtraData(), DefaultHealthCheckExtraData.class);
    Assert.assertEquals("appId", data.getAppId());
    Assert.assertEquals("serviceName", data.getServiceName());
    Assert.assertEquals("0.0.1", data.getServiceVersion());
    Assert.assertEquals("001", data.getInstanceId());
    Assert.assertEquals("localhost", data.getHostName());
    Assert.assertEquals("127.0.0.1,192.168.0.100", data.getEndpoints());

    data = JsonUtils.OBJ_MAPPER
        .readValue(publisher.healthWithName("default").getExtraData(), DefaultHealthCheckExtraData.class);
    Assert.assertEquals("appId", data.getAppId());
    Assert.assertEquals("serviceName", data.getServiceName());
    Assert.assertEquals("0.0.1", data.getServiceVersion());
    Assert.assertEquals("001", data.getInstanceId());
    Assert.assertEquals("localhost", data.getHostName());
    Assert.assertEquals("127.0.0.1,192.168.0.100", data.getEndpoints());
  }
}
