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

package org.apache.servicecomb.localregistry;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;

public class LocalRegistrationStore {
  private Microservice microservice;

  private MicroserviceInstance microserviceInstance;

  public LocalRegistrationStore() {

  }

  public void init() {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    microservice = microserviceFactory.create(microserviceDefinition);
    microserviceInstance = microservice.getInstance();
  }

  public void run() {
    microservice.setServiceId("[local]-[" + microservice.getAppId()
        + "]-[" + microservice.getServiceName() + "]");
    microserviceInstance.setInstanceId(microservice.getServiceId());
    microserviceInstance.setServiceId(microservice.getServiceId());
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public MicroserviceInstance getMicroserviceInstance() {
    return microserviceInstance;
  }
}
