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

package org.apache.servicecomb.registry.nacos;

import java.util.HashMap;
import java.util.Map;

public class NacosDiscoveryProperties {
  private boolean enabled = true;

  private String serverAddr = "127.0.0.1";

  private Map<String, String> metadata = new HashMap<>();

  private boolean ephemeral = true;

  private String username;

  private String password;

  private String accessKey;

  private String secretKey;

  private String namingLoadCacheAtStart = "false";

  private String clusterName = "DEFAULT";

  private float weight = 1;

  private boolean instanceEnabled = true;

  private String logName;

  public String getServerAddr() {
    return serverAddr;
  }

  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public boolean isEphemeral() {
    return ephemeral;
  }

  public void setEphemeral(boolean ephemeral) {
    this.ephemeral = ephemeral;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getNamingLoadCacheAtStart() {
    return namingLoadCacheAtStart;
  }

  public void setNamingLoadCacheAtStart(String namingLoadCacheAtStart) {
    this.namingLoadCacheAtStart = namingLoadCacheAtStart;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public float getWeight() {
    return weight;
  }

  public void setWeight(float weight) {
    this.weight = weight;
  }

  public boolean isInstanceEnabled() {
    return instanceEnabled;
  }

  public void setInstanceEnabled(boolean instanceEnabled) {
    this.instanceEnabled = instanceEnabled;
  }

  public String getLogName() {
    return logName;
  }

  public void setLogName(String logName) {
    this.logName = logName;
  }
}
