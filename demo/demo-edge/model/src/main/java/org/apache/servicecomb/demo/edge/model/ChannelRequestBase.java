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

public class ChannelRequestBase {
  private String deviceId;

  private String serviceToken;

  private String phoneType;

  private String userId;

  private String cmdId;

  private String net;

  private String userGrant;

  private String sysVer;

  private String ts;

  private String channelId;

  private String location;

  private String cmdVer;

  private String version;

  private String language;

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getServiceToken() {
    return serviceToken;
  }

  public void setServiceToken(String serviceToken) {
    this.serviceToken = serviceToken;
  }

  public String getPhoneType() {
    return phoneType;
  }

  public void setPhoneType(String phoneType) {
    this.phoneType = phoneType;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCmdId() {
    return cmdId;
  }

  public void setCmdId(String cmdId) {
    this.cmdId = cmdId;
  }

  public String getNet() {
    return net;
  }

  public void setNet(String net) {
    this.net = net;
  }

  public String getUserGrant() {
    return userGrant;
  }

  public void setUserGrant(String userGrant) {
    this.userGrant = userGrant;
  }

  public String getSysVer() {
    return sysVer;
  }

  public void setSysVer(String sysVer) {
    this.sysVer = sysVer;
  }

  public String getTs() {
    return ts;
  }

  public void setTs(String ts) {
    this.ts = ts;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getCmdVer() {
    return cmdVer;
  }

  public void setCmdVer(String cmdVer) {
    this.cmdVer = cmdVer;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public String toString() {
    return "ChannelRequestBase [deviceId=" + deviceId + ", serviceToken=" + serviceToken + ", phoneType=" + phoneType
        + ", userId=" + userId + ", cmdId=" + cmdId + ", net=" + net + ", userGrant=" + userGrant + ", sysVer=" + sysVer
        + ", ts=" + ts + ", channelId=" + channelId + ", location=" + location + ", cmdVer=" + cmdVer + ", version="
        + version + ", language=" + language + "]";
  }
}
