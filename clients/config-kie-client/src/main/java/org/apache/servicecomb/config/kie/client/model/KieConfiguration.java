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

package org.apache.servicecomb.config.kie.client.model;

public class KieConfiguration {
  private boolean enableLongPolling;

  private int pollingWaitInSeconds;

  private int refreshIntervalInMillis = 15000;

  private String project;

  private String appName;

  private String serviceName;

  private String environment;

  private String version;

  private boolean enableAppConfig;

  private boolean enableServiceConfig;

  private boolean enableVersionConfig;

  private boolean enableCustomConfig;

  private String customLabelValue;

  private String customLabel;

  private boolean firstPullRequired;

  public String getAppName() {
    return appName;
  }

  public KieConfiguration setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getServiceName() {
    return serviceName;
  }

  public KieConfiguration setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public String getEnvironment() {
    return environment;
  }

  public KieConfiguration setEnvironment(String environment) {
    this.environment = environment;
    return this;
  }

  public String getCustomLabelValue() {
    return customLabelValue;
  }

  public KieConfiguration setCustomLabelValue(String customLabelValue) {
    this.customLabelValue = customLabelValue;
    return this;
  }

  public boolean isEnableAppConfig() {
    return enableAppConfig;
  }

  public KieConfiguration setEnableAppConfig(boolean enableAppConfig) {
    this.enableAppConfig = enableAppConfig;
    return this;
  }

  public boolean isEnableServiceConfig() {
    return enableServiceConfig;
  }

  public KieConfiguration setEnableServiceConfig(boolean enableServiceConfig) {
    this.enableServiceConfig = enableServiceConfig;
    return this;
  }

  public boolean isEnableCustomConfig() {
    return enableCustomConfig;
  }

  public KieConfiguration setEnableCustomConfig(boolean enableCustomConfig) {
    this.enableCustomConfig = enableCustomConfig;
    return this;
  }

  public String getCustomLabel() {
    return customLabel;
  }

  public KieConfiguration setCustomLabel(String customLabel) {
    this.customLabel = customLabel;
    return this;
  }

  public boolean isEnableLongPolling() {
    return enableLongPolling;
  }

  public KieConfiguration setEnableLongPolling(boolean enableLongPolling) {
    this.enableLongPolling = enableLongPolling;
    return this;
  }

  public boolean isEnableVersionConfig() {
    return enableVersionConfig;
  }

  public KieConfiguration setEnableVersionConfig(boolean enableVersionConfig) {
    this.enableVersionConfig = enableVersionConfig;
    return this;
  }

  public int getPollingWaitInSeconds() {
    return pollingWaitInSeconds;
  }

  public KieConfiguration setPollingWaitInSeconds(int pollingWaitInSeconds) {
    this.pollingWaitInSeconds = pollingWaitInSeconds;
    return this;
  }

  public String getProject() {
    return project;
  }

  public KieConfiguration setProject(String project) {
    this.project = project;
    return this;
  }

  public boolean isFirstPullRequired() {
    return firstPullRequired;
  }

  public KieConfiguration setFirstPullRequired(boolean firstPullRequired) {
    this.firstPullRequired = firstPullRequired;
    return this;
  }

  public int getRefreshIntervalInMillis() {
    return refreshIntervalInMillis;
  }

  public KieConfiguration setRefreshIntervalInMillis(int refreshIntervallnMillis) {
    this.refreshIntervalInMillis = refreshIntervallnMillis;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public KieConfiguration setVersion(String version) {
    this.version = version;
    return this;
  }
}
