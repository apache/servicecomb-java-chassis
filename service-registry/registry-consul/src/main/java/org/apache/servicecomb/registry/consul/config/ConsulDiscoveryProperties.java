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

import java.util.ArrayList;
import java.util.List;

import com.ecwid.consul.v1.ConsistencyMode;

import org.apache.servicecomb.registry.consul.utils.InetUtils;
import org.springframework.core.style.ToStringCreator;


public class ConsulDiscoveryProperties {

  private HostInfo hostInfo;

  private String aclToken;

  private List<String> tags = new ArrayList<>();

  private Boolean enableTagOverride;

  private boolean enabled = true;

  private String ipAddress;

  private String hostname;

  private Integer port;

  private boolean preferIpAddress = true;

  private boolean preferAgentAddress = false;

  private ConsistencyMode consistencyMode = ConsistencyMode.DEFAULT;

  private Integer delayTime = 10000;

  public ConsulDiscoveryProperties(InetUtils inetUtils) {
    this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
    this.ipAddress = this.hostInfo.getIpAddress();
    this.hostname = this.hostInfo.getHostname();
  }

  public String getHostname() {
    return this.preferIpAddress ? this.ipAddress : this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
    this.hostInfo.setOverride(true);
  }

  private HostInfo getHostInfo() {
    return this.hostInfo;
  }

  private void setHostInfo(HostInfo hostInfo) {
    this.hostInfo = hostInfo;
  }

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

  public boolean isEnableTagOverride() {
    return enableTagOverride;
  }

  public void setEnableTagOverride(boolean enableTagOverride) {
    this.enableTagOverride = enableTagOverride;
  }


  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }


  public String getIpAddress() {
    return this.ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
    this.hostInfo.setOverride(true);
  }

  public Integer getPort() {
    return this.port;
  }

  private boolean enableSwaggerRegistration = false;

  public void setPort(Integer port) {
    this.port = port;
  }

  public boolean isPreferIpAddress() {
    return this.preferIpAddress;
  }

  public void setPreferIpAddress(boolean preferIpAddress) {
    this.preferIpAddress = preferIpAddress;
  }

  public boolean isPreferAgentAddress() {
    return this.preferAgentAddress;
  }

  public void setPreferAgentAddress(boolean preferAgentAddress) {
    this.preferAgentAddress = preferAgentAddress;
  }

  public Boolean getEnableTagOverride() {
    return this.enableTagOverride;
  }

  public void setEnableTagOverride(Boolean enableTagOverride) {
    this.enableTagOverride = enableTagOverride;
  }

  public ConsistencyMode getConsistencyMode() {
    return consistencyMode;
  }

  public void setConsistencyMode(ConsistencyMode consistencyMode) {
    this.consistencyMode = consistencyMode;
  }

  public boolean isEnableSwaggerRegistration() {
    return enableSwaggerRegistration;
  }

  public void setEnableSwaggerRegistration(boolean enableSwaggerRegistration) {
    this.enableSwaggerRegistration = enableSwaggerRegistration;
  }

  public Integer getDelayTime() {
    return delayTime;
  }

  public void setDelayTime(Integer delayTime) {
    this.delayTime = delayTime;
  }

  @Override
  public String toString() {
    return new ToStringCreator(this).append("aclToken", this.aclToken)
        .append("enabled", this.enabled)
        .append("enableTagOverride", this.enableTagOverride)
        .append("hostInfo", this.hostInfo)
        .append("hostname", this.hostname)
        .append("ipAddress", this.ipAddress)
        .append("port", this.port)
        .append("preferAgentAddress", this.preferAgentAddress)
        .append("preferIpAddress", this.preferIpAddress)
        .append("tags", this.tags)
        .toString();
  }
}
