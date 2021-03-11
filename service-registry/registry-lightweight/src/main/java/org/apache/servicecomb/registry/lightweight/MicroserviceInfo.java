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

package org.apache.servicecomb.registry.lightweight;

import java.util.Map;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

public class MicroserviceInfo {
  private Microservice microservice;

  private Map<String, String> schemasById;

  private MicroserviceInstance instance;

  public Microservice getMicroservice() {
    return microservice;
  }

  public MicroserviceInfo setMicroservice(Microservice microservice) {
    this.microservice = microservice;
    return this;
  }

  public Map<String, String> getSchemasById() {
    return schemasById;
  }

  public MicroserviceInfo setSchemasById(Map<String, String> schemasById) {
    this.schemasById = schemasById;
    return this;
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public MicroserviceInfo setInstance(MicroserviceInstance instance) {
    this.instance = instance;
    return this;
  }
}
