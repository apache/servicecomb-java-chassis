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

public abstract class RegistrationEvents {
  protected boolean success;

  protected RegistrationEvents(boolean success) {
    this.success = success;
  }

  public boolean isSuccess() {
    return this.success;
  }

  public static class MicroserviceRegistrationEvent extends RegistrationEvents {
    public MicroserviceRegistrationEvent(boolean success) {
      super(success);
    }
  }

  public static class SchemaRegistrationEvent extends RegistrationEvents {
    public SchemaRegistrationEvent(boolean success) {
      super(success);
    }
  }

  public static class MicroserviceInstanceRegistrationEvent extends RegistrationEvents {
    public MicroserviceInstanceRegistrationEvent(boolean success) {
      super(success);
    }
  }

  public static class HeartBeatEvent extends RegistrationEvents {
    public HeartBeatEvent(boolean success) {
      super(success);
    }
  }
}
