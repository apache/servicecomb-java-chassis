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

package io.servicecomb.foundation.metrics.output.servo;

import org.springframework.stereotype.Component;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;

@Component
public class MicroserviceLoader {

  public Microservice load() {
    try {
      //TODO: has any better way get appId and microserviceName ? new MicroserviceDefinition may heavy cost
      MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
      MicroserviceDefinition definition = new MicroserviceDefinition(loader.getConfigModels());
      Microservice microservice = new Microservice();
      microservice.setServiceName(definition.getMicroserviceName());
      microservice.setAppId(definition.getApplicationId());
      return microservice;
    } catch (Exception e) {
      throw new ServiceCombException("can't get microservice from RegistryUtils", e);
    }
  }
}
