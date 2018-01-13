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

package org.apache.servicecomb.metrics.common;

public class DefaultHealthCheckExtraData {
  private String instanceId;

  private String hostName;

  private String appId;

  private String serviceName;

  private String serviceVersion;

  private String endpoints;

  public String getInstanceId() {
    return instanceId;
  }

  public String getHostName() {
    return hostName;
  }

  public String getAppId() {
    return appId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public String getEndpoints() {
    return endpoints;
  }

  public DefaultHealthCheckExtraData() {
  }

  public DefaultHealthCheckExtraData(String instanceId, String hostName, String appId, String serviceName,
      String serviceVersion, String endpoints) {
    this.instanceId = instanceId;
    this.hostName = hostName;
    this.appId = appId;
    this.serviceName = serviceName;
    this.serviceVersion = serviceVersion;
    this.endpoints = endpoints;
  }
}
