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

package org.apache.servicecomb.registry.api.event;

/**
 * when registration is ready, should post this event.
 */
public class MicroserviceInstanceRegisteredEvent {
  private final String registrationName;

  private final String instanceId;

  // If this event is sent by RegistrationManager, which means all Registration are successful.
  private final boolean registrationManager;

  public MicroserviceInstanceRegisteredEvent(String registrationName, String instanceId,
      boolean registrationManager) {
    this.registrationName = registrationName;
    this.instanceId = instanceId;
    this.registrationManager = registrationManager;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getRegistrationName() {
    return registrationName;
  }

  public boolean isRegistrationManager() {
    return registrationManager;
  }
}
