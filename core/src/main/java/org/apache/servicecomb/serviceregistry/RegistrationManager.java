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

package org.apache.servicecomb.serviceregistry;

import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.api.registry.BasePath;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.swagger.SwaggerLoader;

public class RegistrationManager {
  public static RegistrationManager INSTANCE = new RegistrationManager();

  private List<Registration> registrationList = SPIServiceUtils.getOrLoadSortedService(Registration.class);

  private Registration primary = SPIServiceUtils.getPriorityHighestService(Registration.class);

  private static SwaggerLoader swaggerLoader = new SwaggerLoader();

  public MicroserviceInstance getMicroserviceInstance() {
    return primary.getMicroserviceInstance();
  }

  public Microservice getMicroservice() {
    return primary.getMicroservice();
  }

  public SwaggerLoader getSwaggerLoader() {
    return swaggerLoader;
  }

  public void updateMicroserviceInstanceStatus(
      MicroserviceInstanceStatus status) {
    registrationList
        .forEach(registration -> registration.updateMicroserviceInstanceStatus(status));
  }

  public void addSchema(String schemaId, String content) {
    registrationList
        .forEach(registration -> registration.addSchema(schemaId, content));
  }

  public void addBasePath(Collection<BasePath> basePaths) {
    registrationList
        .forEach(registration -> registration.addBasePath(basePaths));
  }

  public void destroy() {
    registrationList.forEach(registration -> registration.destroy());
  }

  public void run() {
    registrationList.forEach(registration -> registration.run());
  }
}
