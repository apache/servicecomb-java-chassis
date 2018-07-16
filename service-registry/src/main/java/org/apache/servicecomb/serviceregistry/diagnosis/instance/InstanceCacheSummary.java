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
package org.apache.servicecomb.serviceregistry.diagnosis.instance;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.serviceregistry.diagnosis.Status;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InstanceCacheSummary {
  // consumer appId and microserviceName
  @JsonIgnore
  private String appId;

  @JsonIgnore
  private String microserviceName;

  private Status status;

  private List<InstanceCacheResult> producers = new ArrayList<>();

  private long timestamp;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public void setMicroserviceName(String microserviceName) {
    this.microserviceName = microserviceName;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public List<InstanceCacheResult> getProducers() {
    return producers;
  }

  public void setProducers(List<InstanceCacheResult> producers) {
    this.producers = producers;
  }
}
