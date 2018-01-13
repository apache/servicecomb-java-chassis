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

package org.apache.servicecomb.demo.edge.model;

import org.apache.servicecomb.serviceregistry.RegistryUtils;

public class ResultWithInstance {
  private int result;

  private String serviceId;

  private String instanceId;

  private String version;

  public static ResultWithInstance create(int value) {
    ResultWithInstance result = new ResultWithInstance();
    result.setResult(value);
    result.setInstanceId(RegistryUtils.getMicroserviceInstance().getInstanceId());
    result.setServiceId(RegistryUtils.getMicroservice().getServiceId());
    result.setVersion(RegistryUtils.getMicroservice().getVersion());

    return result;
  }

  public int getResult() {
    return result;
  }

  public void setResult(int result) {
    this.result = result;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "ResultWithInstance [result=" + result + ", serviceId=" + serviceId + ", instanceId=" + instanceId
        + ", version=" + version + "]";
  }
}
