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
import java.util.Objects;
import java.util.Properties;

public class NacosDiscoveryProperties {
  private boolean enabled = true;

  private String serverAddr;

  private String ip;

  private String namespace;

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

  private boolean secure;

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

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
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

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public Properties getProperties() {
    Properties properties = new Properties();
    properties.put(NacosConst.SERVER_ADDR, this.serverAddr);
    properties.put(NacosConst.USERNAME, Objects.toString(this.username, ""));
    properties.put(NacosConst.PASSWORD, Objects.toString(this.password, ""));
    properties.put(NacosConst.NAMESPACE, Objects.toString(this.namespace, ""));
    properties.put(NacosConst.NACOS_NAMING_LOG_NAME, Objects.toString(this.logName, ""));
    properties.put(NacosConst.ACCESS_KEY, Objects.toString(this.accessKey, ""));
    properties.put(NacosConst.SECRET_KEY, Objects.toString(this.secretKey, ""));
    properties.put(NacosConst.CLUSTER_NAME, this.clusterName);
    properties.put(NacosConst.NAMING_LOAD_CACHE_AT_START, this.namingLoadCacheAtStart);
    return properties;
  }
}
