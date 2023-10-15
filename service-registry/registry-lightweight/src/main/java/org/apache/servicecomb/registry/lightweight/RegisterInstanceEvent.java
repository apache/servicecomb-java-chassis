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

import org.apache.servicecomb.registry.lightweight.model.Microservice;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstance;

/**
 * post when a new instance registered<br>
 * to notify registration send a new register message<br>
 * currently, only "zero config" need this
 */
public class RegisterInstanceEvent {
  private final Microservice microservice;

  private final MicroserviceInstance instance;

  public RegisterInstanceEvent(Microservice microservice, MicroserviceInstance instance) {
    this.microservice = microservice;
    this.instance = instance;
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }
}
