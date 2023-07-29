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

package org.apache.servicecomb.registry.lightweight.store;

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstances;

public class AppStore {
  private final Map<String, MicroserviceStore> microserviceByName = new ConcurrentHashMapEx<>();

  public void addMicroservice(MicroserviceStore microserviceStore) {
    microserviceByName.put(microserviceStore.getServiceName(), microserviceStore);
  }

  public MicroserviceInstances findServiceInstances(String serviceName, String revision) {
    MicroserviceStore microserviceStore = microserviceByName.get(serviceName);
    if (microserviceStore == null) {
      return new MicroserviceInstances()
          .setMicroserviceNotExist(true);
    }

    return microserviceStore.findServiceInstances(revision);
  }
}
