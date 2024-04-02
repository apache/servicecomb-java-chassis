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
package org.apache.servicecomb.registry.zookeeper;

public class ZookeeperRegistryProperties {
  private boolean enabled = true;

  private boolean ephemeral = true;

  private String connectString = "zookeeper://127.0.0.1:2181";

  private String authenticationSchema;

  private String authenticationInfo;

  private int connectionTimeoutMillis = 1000;

  private int sessionTimeoutMillis = 60000;

  private boolean enableSwaggerRegistration = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEphemeral() {
    return ephemeral;
  }

  public void setEphemeral(boolean ephemeral) {
    this.ephemeral = ephemeral;
  }

  public String getConnectString() {
    return connectString;
  }

  public void setConnectString(String connectString) {
    this.connectString = connectString;
  }

  public int getConnectionTimeoutMillis() {
    return connectionTimeoutMillis;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public int getSessionTimeoutMillis() {
    return sessionTimeoutMillis;
  }

  public void setSessionTimeoutMillis(int sessionTimeoutMillis) {
    this.sessionTimeoutMillis = sessionTimeoutMillis;
  }

  public boolean isEnableSwaggerRegistration() {
    return enableSwaggerRegistration;
  }

  public void setEnableSwaggerRegistration(boolean enableSwaggerRegistration) {
    this.enableSwaggerRegistration = enableSwaggerRegistration;
  }

  public String getAuthenticationSchema() {
    return authenticationSchema;
  }

  public void setAuthenticationSchema(String authenticationSchema) {
    this.authenticationSchema = authenticationSchema;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }
}
