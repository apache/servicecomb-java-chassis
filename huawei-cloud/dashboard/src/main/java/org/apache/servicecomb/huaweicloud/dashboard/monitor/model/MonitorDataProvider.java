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

package org.apache.servicecomb.huaweicloud.dashboard.monitor.model;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.dashboard.client.model.MonitorData;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.CPUMonitorCalc;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;

public interface MonitorDataProvider {
  boolean enabled();

  default String getURL() {
    return String.format(MonitorConstant.MONITORS_URI, RegistryUtils.getMicroservice().getServiceName());
  }

  default void extractServiceInfo(MonitorData monitorData) {
    Microservice microservice = RegistryUtils.getMicroservice();
    MicroserviceInstance microserviceInstance = RegistryUtils.getMicroserviceInstance();
    monitorData.setAppId(microservice.getAppId());
    monitorData.setName(microservice.getServiceName());
    monitorData.setVersion(microservice.getVersion());
    monitorData.setServiceId(microservice.getServiceId());
    monitorData.setInstance(microserviceInstance.getHostName());
    monitorData.setInstanceId(microserviceInstance.getInstanceId());
    monitorData.setEnvironment(microservice.getEnvironment());
  }

  default void exactProcessInfo(MonitorData monitorData) {
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

  void extractInterfaceInfo(MonitorData monitorData);

  default MonitorData getData() {
    MonitorData monitorData = new MonitorData();
    extractServiceInfo(monitorData);
    exactProcessInfo(monitorData);
    extractInterfaceInfo(monitorData);
    return monitorData;
  }
}
