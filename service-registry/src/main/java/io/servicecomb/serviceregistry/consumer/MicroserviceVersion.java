/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.consumer;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.version.Version;

public class MicroserviceVersion {
  protected Version version;

  protected Microservice microservice;

  public MicroserviceVersion(String microserviceId) {
    microservice = RegistryUtils.getMicroservice(microserviceId);
    if (microservice == null) {
      throw new IllegalStateException(String.format("Invalid microserviceId %s.", microserviceId));
    }

    this.version = new Version(microservice.getVersion());
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public Version getVersion() {
    return version;
  }
}
