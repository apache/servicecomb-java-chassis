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

package io.servicecomb.metrics.core.health;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.metrics.common.DefaultHealthCheckExtraData;
import io.servicecomb.metrics.common.HealthCheckResult;
import io.servicecomb.metrics.common.HealthChecker;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class DefaultMicroserviceHealthChecker implements HealthChecker {

  @Override
  public String getName() {
    return "default";
  }

  @Override
  public HealthCheckResult check() {
    return new HealthCheckResult(true, "", getExtraData());
  }

  private String getExtraData() {
    try {
      if (RegistryUtils.getServiceRegistry() == null) {
        RegistryUtils.init();
      }

      Microservice microservice = RegistryUtils.getMicroservice();
      MicroserviceInstance instance = RegistryUtils.getMicroserviceInstance();
      return JsonUtils.writeValueAsString(new DefaultHealthCheckExtraData(
          instance.getInstanceId(),
          instance.getHostName(),
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          String.join(",", instance.getEndpoints())));
    } catch (JsonProcessingException e) {
      throw new ServiceCombException("unable load microservice info for healthchecker", e);
    }
  }
}
