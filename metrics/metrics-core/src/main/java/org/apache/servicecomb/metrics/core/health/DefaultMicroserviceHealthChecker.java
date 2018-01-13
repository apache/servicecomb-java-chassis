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

package org.apache.servicecomb.metrics.core.health;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.metrics.common.DefaultHealthCheckExtraData;
import org.apache.servicecomb.metrics.common.HealthCheckResult;
import org.apache.servicecomb.metrics.common.HealthChecker;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultMicroserviceHealthChecker implements HealthChecker {

  private static Logger logger = LoggerFactory.getLogger(DefaultMicroserviceHealthChecker.class);

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
      Microservice microservice = RegistryUtils.getMicroservice();
      MicroserviceInstance instance = RegistryUtils.getMicroserviceInstance();
      return JsonUtils.writeValueAsString(new DefaultHealthCheckExtraData(
          instance.getInstanceId(),
          instance.getHostName(),
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          String.join(",", instance.getEndpoints())));
    } catch (Exception e) {
      String error = "unable load microservice info from RegistryUtils";
      logger.error(error, e);
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR, error);
    }
  }
}
