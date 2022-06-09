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

package org.apache.servicecomb.huaweicloud.dashboard.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.servicecomb.dashboard.client.model.InterfaceInfo;
import org.apache.servicecomb.dashboard.client.model.MonitorData;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.CPUMonitorCalc;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDaraProvider;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.diagnosis.instance.InstanceCacheSummary;

import com.google.common.eventbus.Subscribe;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;

public class HealthMonitorDataProvider implements MonitorDaraProvider {
  private InstanceCacheSummary instanceCacheSummary;

  private final Object lock = new Object();

  @Override
  public String getURL() {
    return String.format(MonitorConstant.MONITORS_URI, RegistryUtils.getMicroservice().getServiceName());
  }

  @Override
  public MonitorData getData() {
    return getMonitorData();
  }

  @PostConstruct
  public void init() {
    EventManager.register(this);
  }

  @Subscribe
  public void subCacheCheck(InstanceCacheSummary instanceCacheSummary) {
    synchronized (lock) {
      this.instanceCacheSummary = instanceCacheSummary;
    }
  }

  private MonitorData getMonitorData() {
    Collection<HystrixCommandMetrics> instances = HystrixCommandMetrics.getInstances();
    MonitorData monitorData = new MonitorData();
    Microservice microservice = RegistryUtils.getMicroservice();
    MicroserviceInstance microserviceInstance = RegistryUtils.getMicroserviceInstance();
    monitorData.setAppId(microservice.getAppId());
    monitorData.setName(microservice.getServiceName());
    monitorData.setVersion(microservice.getVersion());
    monitorData.setServiceId(microservice.getServiceId());
    monitorData.setInstance(microserviceInstance.getHostName());
    monitorData.setInstanceId(microserviceInstance.getInstanceId());
    exactProcessInfo(monitorData);
    if (instances.isEmpty()) {
      return monitorData;
    }
    for (HystrixCommandMetrics instance : instances) {
      appendInterfaceInfo(monitorData, instance);
    }
    return monitorData;
  }

  private void exactProcessInfo(MonitorData monitorData) {
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage memoryHeapUsage = memoryMXBean.getHeapMemoryUsage();
    MemoryUsage memoryNonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    int threadCount = threadMXBean.getThreadCount();
    OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    double cpu = operatingSystemMXBean.getSystemLoadAverage();
    monitorData.setCpu(CPUMonitorCalc.getInstance().getProcessCpu());
    monitorData.setLoadAverage(cpu);
    monitorData.setThreadCount(threadCount);
    monitorData.setUptime(runtimeMXBean.getUptime());

    Map<String, Long> memoryInfo = new HashMap<>();
    memoryInfo.put("heapInit", memoryHeapUsage.getInit());
    memoryInfo.put("headMax", memoryHeapUsage.getMax());
    memoryInfo.put("heapCommit", memoryHeapUsage.getCommitted());
    memoryInfo.put("heapUsed", memoryHeapUsage.getUsed());
    memoryInfo.put("nonHeapInit", memoryNonHeapUsage.getInit());
    memoryInfo.put("nonHeapCommit", memoryNonHeapUsage.getCommitted());
    memoryInfo.put("nonHeapUsed", memoryNonHeapUsage.getUsed());
    monitorData.setMemory(memoryInfo);
  }

  public void appendInterfaceInfo(MonitorData monitorData, HystrixCommandMetrics metrics) {
    InterfaceInfo interfaceInfo = new InterfaceInfo();
    int windowTime = metrics.getProperties().metricsRollingStatisticalWindowInMilliseconds().get() / MonitorData.CONVERSION;
    long successCount = metrics.getRollingCount(HystrixEventType.SUCCESS);
    long failureCount = metrics.getRollingCount(HystrixEventType.FAILURE);
    long semRejectCount = metrics.getRollingCount(HystrixEventType.SEMAPHORE_REJECTED);
    long threadRejectCount = metrics.getRollingCount(HystrixEventType.THREAD_POOL_REJECTED);
    long timeoutCount = metrics.getRollingCount(HystrixEventType.TIMEOUT);
    long shortCircuitedCount = metrics.getRollingCount(HystrixEventType.SHORT_CIRCUITED);
    long rollingErrorTotal = failureCount + semRejectCount + threadRejectCount + timeoutCount;
    long rollingTotal = successCount + rollingErrorTotal;

    if (rollingTotal == 0) {
      interfaceInfo.setRate(MonitorData.DEFAULT_SUCCESS_RATE);
      interfaceInfo.setFailureRate(0d);
    } else {
      double failurePercentage = (double) rollingErrorTotal / rollingTotal;
      interfaceInfo.setRate(MonitorData.DEFAULT_SUCCESS_RATE - failurePercentage);
      interfaceInfo.setFailureRate(failurePercentage);
    }

    int latency = metrics.getTotalTimeMean();
    int latency995 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_995);
    int latency99 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_99);
    int latency90 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_90);
    int latency75 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_75);
    int latency50 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_50);
    int latency25 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_25);
    int latency5 = metrics.getTotalTimePercentile(MonitorData.PERCENTAGE_5);

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
    BigDecimal qps = b.setScale(MonitorData.SCALE_VAL, RoundingMode.HALF_DOWN);
    interfaceInfo.setQps(qps.doubleValue());
    monitorData.addInterfaceInfo(interfaceInfo);
  }

  private boolean isOpen(HystrixCommandMetrics metrics) {
    if (metrics.getProperties().circuitBreakerForceOpen().get()) {
      return true;
    }
    if (metrics.getProperties().circuitBreakerForceClosed().get()) {
      return false;
    }
    HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(metrics.getCommandKey());
    return circuitBreaker != null && circuitBreaker.isOpen();
  }
}
