/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.servicecomb.metrics.core.health.HealthCheckResult;
import io.servicecomb.metrics.core.health.MicroserviceInstanceData;
import io.servicecomb.metrics.core.registry.DefaultHealthCheckRegistry;
import io.servicecomb.metrics.core.registry.HealthCheckRegistry;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import mockit.Expectations;

public class TestDefaultHealthCheckRegistry {

  @Test
  public void testRegistry() {

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

    HealthCheckRegistry registry = new DefaultHealthCheckRegistry();
    Map<String, HealthCheckResult> results = registry.checkAllStatus();

    Assert.assertTrue(results.get("default").isHealth());

    MicroserviceInstanceData data = (MicroserviceInstanceData) results.get("default").getExtraData();
    Assert.assertTrue(data.getAppId().equals("appId"));
    Assert.assertTrue(data.getServiceName().equals("serviceName"));
    Assert.assertTrue(data.getServiceVersion().equals("0.0.1"));
    Assert.assertTrue(data.getInstanceId().equals("001"));
    Assert.assertTrue(data.getHostName().equals("localhost"));
    Assert.assertTrue(data.getEndpoints().equals("127.0.0.1,192.168.0.100"));



  }
}
