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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.List;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class StaticMicroserviceVersions extends MicroserviceVersions {

  private Class<?> schemaIntfCls;

  public StaticMicroserviceVersions(AppManager appManager, String appId, String microserviceName,
      Class<?> schemaIntfCls) {
    super(appManager, appId, microserviceName);

    validated = true;
    this.schemaIntfCls = schemaIntfCls;
  }

  @Override
  public void pullInstances() {
    // instance information is stored locally, do not pull from sc
  }

  public void addInstances(String version, List<MicroserviceInstance> instances) {

  }
}
