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

package org.apache.servicecomb.registry.consul.config;

import org.springframework.core.style.ToStringCreator;

import java.util.ArrayList;
import java.util.List;


public class ConsulDiscoveryProperties {

  private String aclToken;

  private List<String> tags = new ArrayList<>();

  private Boolean enabled = true;

  private Integer watchSeconds = 8;

  private Boolean enableSwaggerRegistration = false;

  private String serviceId;

  public String getAclToken() {
    return this.aclToken;
  }

  public void setAclToken(String aclToken) {
    this.aclToken = aclToken;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public Boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean isEnableSwaggerRegistration() {
    return enableSwaggerRegistration;
  }

  public void setEnableSwaggerRegistration(Boolean enableSwaggerRegistration) {
    this.enableSwaggerRegistration = enableSwaggerRegistration;
  }

  public Integer getWatchSeconds() {
    return watchSeconds;
  }

  public void setWatchSeconds(Integer watchSeconds) {
    this.watchSeconds = watchSeconds;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public String toString() {
    return new ToStringCreator(this).append("aclToken", this.aclToken)
        .append("enabled", this.enabled)
        .append("serviceId", this.serviceId)
        .append("tags", this.tags)
        .append("watchSeconds", this.watchSeconds)
        .toString();
  }
}
