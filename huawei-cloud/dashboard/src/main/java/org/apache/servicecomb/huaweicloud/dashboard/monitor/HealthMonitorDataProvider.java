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

import com.google.common.eventbus.Subscribe;
import com.netflix.hystrix.HystrixCommandMetrics;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.CPUMonitorCalc;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.Diagnosis;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorData;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDaraProvider;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.diagnosis.instance.InstanceCacheChecker;
import org.apache.servicecomb.serviceregistry.diagnosis.instance.InstanceCacheSummary;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HealthMonitorDataProvider implements MonitorDaraProvider {

  private InstanceCacheSummary instanceCacheSummary;

  private final Object lock = new Object();

  @Override
  public String getURL() {
    return String.format(MonitorConstant.MONITORS_URI, RegistryUtils.getMicroservice().getServiceName());
  }

  @Override
  public Object getData() {
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

    if (MonitorConstant.insCacheEnabled()) {
      InstanceCacheChecker checker = new InstanceCacheChecker(DiscoveryManager.INSTANCE.getAppManager());
      monitorData.setProvidersCache(checker.check().getProducers());
    }
    exactProcessInfo(monitorData);
    synchronized (lock) {
      if (this.instanceCacheSummary != null) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setInstanceCache(instanceCacheSummary);
        monitorData.setDiagnosis(diagnosis);
        this.instanceCacheSummary = null;
      }
    }

    if (instances.isEmpty()) {
      return monitorData;
    }
    for (HystrixCommandMetrics instance : instances) {
      monitorData.appendInterfaceInfo(instance);
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
}
