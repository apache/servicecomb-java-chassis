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

package org.apache.servicecomb.dashboard.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitorData {
  public static final double PERCENTAGE_995 = 99.5;

  public static final double PERCENTAGE_99 = 99;

  public static final double PERCENTAGE_90 = 90;

  public static final double PERCENTAGE_75 = 75;

  public static final double PERCENTAGE_50 = 50;

  public static final double PERCENTAGE_25 = 25;

  public static final double PERCENTAGE_5 = 5;

  public static final int SCALE_VAL = 1;

  public static final double DEFAULT_SUCCESS_RATE = 1.0d;

  public static final int CONVERSION = 1000;

  private String appId;

  private String version;

  private String name;

  private String serviceId;

  private String environment;

  private String instance;

  private String instanceId;

  private int thread;

  private double cpu;

  private double loadAverage;

  private long uptime;

  private Map<String, Long> memory;

  private List<InterfaceInfo> interfaces = new ArrayList<>();

  private Map<String, Object> customs;

  public void addInterfaceInfo(InterfaceInfo interfaceInfo) {
    interfaces.add(interfaceInfo);
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int getThreadCount() {
    return thread;
  }

  public void setThreadCount(int threadCount) {
    this.thread = threadCount;
  }

  public double getCpu() {
    return cpu;
  }

  public void setCpu(double cpu) {
    this.cpu = cpu;
  }

  public double getLoadAverage() {
    return loadAverage;
  }

  public void setLoadAverage(double loadAverage) {
    this.loadAverage = loadAverage;
  }

  public long getUptime() {
    return uptime;
  }

  public void setUptime(long uptime) {
    this.uptime = uptime;
  }

  public Map<String, Long> getMemory() {
    return memory;
  }

  public void setMemory(Map<String, Long> memory) {
    this.memory = memory;
  }

  public List<InterfaceInfo> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<InterfaceInfo> interfaces) {
    this.interfaces = interfaces;
  }

  public Map<String, Object> getCustoms() {
    return customs;
  }

  public void setCustoms(Map<String, Object> customs) {
    this.customs = customs;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }
}
