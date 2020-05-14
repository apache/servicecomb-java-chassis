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
package org.apache.servicecomb.serviceregistry.adapter;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class CasEnvAdapterOne implements EnvAdapter {
  private static final String NAME = "cas_env_one";

  @Override
  public String getEnvName() {
    return NAME;
  }

  @Override
  public int getOrder() {
    return 10;
  }

  @Override
  public void beforeRegisterService(Microservice microservice) {
    microservice.getProperties().put(NAME, "order=10");
  }

  @Override
  public void beforeRegisterInstance(MicroserviceInstance instance) {
    instance.getProperties().put(NAME, "order=10");
  }
}
