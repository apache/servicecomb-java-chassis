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

import java.util.List;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

public abstract class DiscoveryEvents {
  public static class InstanceChangedEvent extends DiscoveryEvents {
    private String appName;

    private String serviceName;

    private List<MicroserviceInstance> instances;

    public InstanceChangedEvent(String appName, String serviceName, List<MicroserviceInstance> instances) {
      this.appName = appName;
      this.serviceName = serviceName;
      this.instances = instances;
    }

    public String getAppName() {
      return appName;
    }

    public String getServiceName() {
      return serviceName;
    }

    public List<MicroserviceInstance> getInstances() {
      return instances;
    }
  }

  /**
   * internal events to ask for a immediate instance pull
   */
  public static class PullInstanceEvent extends DiscoveryEvents {

  }
}
