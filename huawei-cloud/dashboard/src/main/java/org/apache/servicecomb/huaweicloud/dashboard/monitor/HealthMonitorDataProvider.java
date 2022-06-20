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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import org.apache.servicecomb.dashboard.client.model.InterfaceInfo;
import org.apache.servicecomb.dashboard.client.model.MonitorData;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataProvider;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;

/**
 * This provider depends on bizkeeper. Because Bizkeeper depends on Hystrix
 * and it is not in maintainence, will keep it here for compatible reason.
 */
public class HealthMonitorDataProvider implements MonitorDataProvider {
  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.monitor.provider.bizkeeper.enabled", false)
        .get();
  }

  @Override
  public void extractInterfaceInfo(MonitorData monitorData) {
    Collection<HystrixCommandMetrics> instances = HystrixCommandMetrics.getInstances();
    if (instances.isEmpty()) {
      return;
    }
    for (HystrixCommandMetrics instance : instances) {
      appendInterfaceInfo(monitorData, instance);
    }
  }

  private void appendInterfaceInfo(MonitorData monitorData, HystrixCommandMetrics metrics) {
    InterfaceInfo interfaceInfo = new InterfaceInfo();
    int windowTime =
        metrics.getProperties().metricsRollingStatisticalWindowInMilliseconds().get() / MonitorData.CONVERSION;
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
