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
package org.apache.servicecomb.registry.sc;

/**
 * Configurations for Service Center registration and discovery.
 */
public class SCConfigurationProperties {
  private boolean enabled = true;

  private String address = "http://localhost:30100";

  /**
   * for registration service
   * when swagger is different between local with remote serviceCenter. if ignoreSwaggerDifferent is true.
   * it will ignore the different and continue the program. otherwise, the program will stop.
   */
  private boolean ignoreSwaggerDifferent;

  private boolean canOverwriteSwagger = true;

  private boolean allowCrossApp = false;

  private String hostname;

  private int healthCheckInterval = 15;

  private int healthCheckTimes = 3;

  private int healthCheckRequestTimeout = 5000;

  private int pollInterval = 15000;

  private boolean autoDiscovery = false;

  private String initialStatus = "STARTING";

  private boolean watch = false;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isIgnoreSwaggerDifferent() {
    return ignoreSwaggerDifferent;
  }

  public void setIgnoreSwaggerDifferent(boolean ignoreSwaggerDifferent) {
    this.ignoreSwaggerDifferent = ignoreSwaggerDifferent;
  }

  public boolean isCanOverwriteSwagger() {
    return canOverwriteSwagger;
  }

  public void setCanOverwriteSwagger(boolean canOverwriteSwagger) {
    this.canOverwriteSwagger = canOverwriteSwagger;
  }

  public boolean isAllowCrossApp() {
    return allowCrossApp;
  }

  public void setAllowCrossApp(boolean allowCrossApp) {
    this.allowCrossApp = allowCrossApp;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getHealthCheckInterval() {
    return healthCheckInterval;
  }

  public void setHealthCheckInterval(int healthCheckInterval) {
    this.healthCheckInterval = healthCheckInterval;
  }

  public int getHealthCheckRequestTimeout() {
    return healthCheckRequestTimeout;
  }

  public void setHealthCheckRequestTimeout(int healthCheckRequestTimeout) {
    this.healthCheckRequestTimeout = healthCheckRequestTimeout;
  }

  public int getPollInterval() {
    return pollInterval;
  }

  public void setPollInterval(int pollInterval) {
    this.pollInterval = pollInterval;
  }

  public boolean isAutoDiscovery() {
    return autoDiscovery;
  }

  public void setAutoDiscovery(boolean autoDiscovery) {
    this.autoDiscovery = autoDiscovery;
  }

  public int getHealthCheckTimes() {
    return healthCheckTimes;
  }

  public void setHealthCheckTimes(int healthCheckTimes) {
    this.healthCheckTimes = healthCheckTimes;
  }

  public String getInitialStatus() {
    return initialStatus;
  }

  public void setInitialStatus(String initialStatus) {
    this.initialStatus = initialStatus;
  }

  public boolean isWatch() {
    return watch;
  }

  public void setWatch(boolean watch) {
    this.watch = watch;
  }
}
