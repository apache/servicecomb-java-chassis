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

package org.apache.servicecomb.huaweicloud.dashboard.monitor.data;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;
import org.apache.servicecomb.serviceregistry.diagnosis.instance.InstanceCacheResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitorData {
  private static final double PERCENTAGE_995 = 99.5;

  private static final double PERCENTAGE_99 = 99;

  private static final double PERCENTAGE_90 = 90;

  private static final double PERCENTAGE_75 = 75;

  private static final double PERCENTAGE_50 = 50;

  private static final double PERCENTAGE_25 = 25;

  private static final double PERCENTAGE_5 = 5;

  private static final int SCALE_VAL = 1;

  private static final double DEFAULT_SUCCESS_RATE = 1.0d;

  private String appId;

  private String version;

  private String name;

  private String serviceId;

  private String instance;

  private String instanceId;

  private int thread;

  private double cpu;

  private double loadAverage;

  private long uptime;

  private Map<String, Long> memory;

  private List<InterfaceInfo> interfaces = new ArrayList<>();

  private Diagnosis diagnosis;

  private Map<String, Object> customs;

  private static final int CONVERSION = 1000;

  private List<InstanceCacheResult> providersCache;

  public void appendInterfaceInfo(HystrixCommandMetrics metrics) {
    InterfaceInfo interfaceInfo = new InterfaceInfo();
    int windowTime = metrics.getProperties().metricsRollingStatisticalWindowInMilliseconds().get() / CONVERSION;
    long successCount = metrics.getRollingCount(HystrixEventType.SUCCESS);
    long failureCount = metrics.getRollingCount(HystrixEventType.FAILURE);
    long semRejectCount = metrics.getRollingCount(HystrixEventType.SEMAPHORE_REJECTED);
    long threadRejectCount = metrics.getRollingCount(HystrixEventType.THREAD_POOL_REJECTED);
    long timeoutCount = metrics.getRollingCount(HystrixEventType.TIMEOUT);
    long shortCircuitedCount = metrics.getRollingCount(HystrixEventType.SHORT_CIRCUITED);
    long rollingErrorTotal = failureCount + semRejectCount + threadRejectCount + timeoutCount;
    long rollingTotal = successCount + rollingErrorTotal;

    if (rollingTotal == 0) {
      interfaceInfo.setRate(DEFAULT_SUCCESS_RATE);
      interfaceInfo.setFailureRate(0d);
    } else {
      double failurePercentage = (double) rollingErrorTotal / rollingTotal;
      interfaceInfo.setRate(DEFAULT_SUCCESS_RATE - failurePercentage);
      interfaceInfo.setFailureRate(failurePercentage);
    }

    int latency = metrics.getTotalTimeMean();
    int latency995 = metrics.getTotalTimePercentile(PERCENTAGE_995);
    int latency99 = metrics.getTotalTimePercentile(PERCENTAGE_99);
    int latency90 = metrics.getTotalTimePercentile(PERCENTAGE_90);
    int latency75 = metrics.getTotalTimePercentile(PERCENTAGE_75);
    int latency50 = metrics.getTotalTimePercentile(PERCENTAGE_50);
    int latency25 = metrics.getTotalTimePercentile(PERCENTAGE_25);
    int latency5 = metrics.getTotalTimePercentile(PERCENTAGE_5);

    interfaceInfo.setName(metrics.getCommandKey().name());
    interfaceInfo.setCircuitBreakerOpen(isOpen(metrics));
    interfaceInfo.setShortCircuited(shortCircuitedCount);
    interfaceInfo.setFailureRate(failureCount);
    interfaceInfo.setSemaphoreRejected(semRejectCount);
    interfaceInfo.setThreadPoolRejected(threadRejectCount);
    interfaceInfo.setCountTimeout(timeoutCount);
    interfaceInfo.setDesc(metrics.getCommandKey().name());
    interfaceInfo.setLatency(latency);
    interfaceInfo.setL995(latency995);
    interfaceInfo.setL99(latency99);
    interfaceInfo.setL90(latency90);
    interfaceInfo.setL75(latency75);
    interfaceInfo.setL50(latency50);
    interfaceInfo.setL25(latency25);
    interfaceInfo.setL5(latency5);
    interfaceInfo.setTotal(rollingTotal);
    double qpsVal = ((double) rollingTotal) / windowTime;
    BigDecimal b = new BigDecimal(qpsVal);
    BigDecimal qps = b.setScale(SCALE_VAL, RoundingMode.HALF_DOWN);
    interfaceInfo.setQps(qps.doubleValue());
    interfaces.add(interfaceInfo);
  }

  public boolean isOpen(HystrixCommandMetrics metrics) {
    if (metrics.getProperties().circuitBreakerForceOpen().get()) {
      return true;
    }
    if (metrics.getProperties().circuitBreakerForceClosed().get()) {
      return false;
    }
    HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(metrics.getCommandKey());
    return circuitBreaker != null && circuitBreaker.isOpen();
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

  public Diagnosis getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(Diagnosis diagnosis) {
    this.diagnosis = diagnosis;
  }

  public Map<String, Object> getCustoms() {
    return customs;
  }

  public void setCustoms(Map<String, Object> customs) {
    this.customs = customs;
  }

  public List<InstanceCacheResult> getProvidersCache() {
    return providersCache;
  }

  public void setProvidersCache(List<InstanceCacheResult> providersCache) {
    this.providersCache = providersCache;
  }
}
