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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.metrics.common.DefaultHealthCheckExtraData;
import org.apache.servicecomb.metrics.common.HealthCheckResult;
import org.apache.servicecomb.metrics.common.HealthChecker;
import org.apache.servicecomb.metrics.core.health.DefaultMicroserviceHealthChecker;
import org.apache.servicecomb.metrics.core.publish.DefaultHealthCheckerManager;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerManager;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;

public class TestHealthCheckerManager {

  @Test
  public void testRegistry() throws IOException {

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

    List<HealthChecker> checkers = new ArrayList<>();
    checkers.add(new DefaultMicroserviceHealthChecker());

    HealthCheckerManager manager = new DefaultHealthCheckerManager(checkers);

    manager.register(new HealthChecker() {
      @Override
      public String getName() {
        return "test";
      }

      @Override
      public HealthCheckResult check() {
        return new HealthCheckResult(false, "bad", "bad");
      }
    });

    Map<String, HealthCheckResult> results = manager.check();

    Assert.assertEquals(true,results.get("default").isHealthy());

    DefaultHealthCheckExtraData data = JsonUtils.OBJ_MAPPER
        .readValue(results.get("default").getExtraData(), DefaultHealthCheckExtraData.class);
    Assert.assertEquals("appId",data.getAppId());
    Assert.assertEquals("serviceName",data.getServiceName());
    Assert.assertEquals("0.0.1",data.getServiceVersion());
    Assert.assertEquals("001",data.getInstanceId());
    Assert.assertEquals("localhost",data.getHostName());
    Assert.assertEquals("127.0.0.1,192.168.0.100",data.getEndpoints());

    HealthCheckResult result = manager.check("test");
    Assert.assertEquals(false,result.isHealthy());
  }
}
