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

package org.apache.servicecomb.service.center.client;

import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

public abstract class RegistrationEvents {
  protected final boolean success;

  protected final Microservice microservice;

  protected RegistrationEvents(boolean success, Microservice microservice) {
    this.success = success;
    this.microservice = microservice;
  }

  public boolean isSuccess() {
    return this.success;
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public static class MicroserviceRegistrationEvent extends RegistrationEvents {
    public MicroserviceRegistrationEvent(boolean success, Microservice microservice) {
      super(success, microservice);
    }
  }

  public static class SchemaRegistrationEvent extends RegistrationEvents {
    public SchemaRegistrationEvent(boolean success, Microservice microservice) {
      super(success, microservice);
    }
  }

  public static class MicroserviceInstanceRegistrationEvent extends RegistrationEvents {
    protected final MicroserviceInstance microserviceInstance;

    public MicroserviceInstanceRegistrationEvent(boolean success, Microservice microservice,
        MicroserviceInstance microserviceInstance) {
      super(success, microservice);
      this.microserviceInstance = microserviceInstance;
    }
  }

  public static class HeartBeatEvent extends RegistrationEvents {
    protected final MicroserviceInstance microserviceInstance;

    public HeartBeatEvent(boolean success, Microservice microservice,
        MicroserviceInstance microserviceInstance) {
      super(success, microservice);
      this.microserviceInstance = microserviceInstance;
    }
  }
}
