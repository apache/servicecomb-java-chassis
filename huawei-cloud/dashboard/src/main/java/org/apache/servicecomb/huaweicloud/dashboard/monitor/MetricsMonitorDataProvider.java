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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.dashboard.client.model.InterfaceInfo;
import org.apache.servicecomb.dashboard.client.model.MonitorData;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataProvider;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.PublishModelFactory;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.apache.servicecomb.registry.sc.SCRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.Subscribe;
import com.netflix.spectator.api.Meter;

/**
 * Monitor data based on metrics-core module.
 */
public class MetricsMonitorDataProvider implements MonitorDataProvider {

  public static final String CODE_SUCCESS = "2[0-9]{2}";

  public static final String CODE_TIMEOUT = "408";

  public static final String NAME_PROVIDER = "Provider.";

  public static final String NAME_CONSUMER = "Consumer.";

  private volatile List<Meter> meters = null;

  private SCRegistration scRegistration;

  private Environment environment;

  private MonitorConstant monitorConstant;

  public MetricsMonitorDataProvider() {
    EventManager.register(this);
  }

  @Autowired
  public void setSCRegistration(SCRegistration scRegistration) {
    this.scRegistration = scRegistration;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  public void setMonitorConstant(MonitorConstant monitorConstant) {
    this.monitorConstant = monitorConstant;
  }

  @Override
  public boolean enabled() {
    return environment.getProperty("servicecomb.monitor.provider.metrics.enabled", boolean.class, true);
  }

  @Override
  public String getURL() {
    return String.format(monitorConstant.getMonitorUri(), scRegistration.getMicroserviceInstance().getServiceName());
  }

  @Override
  public void extractServiceInfo(MonitorData monitorData) {
    monitorData.setAppId(scRegistration.getMicroserviceInstance().getApplication());
    monitorData.setName(scRegistration.getMicroserviceInstance().getServiceName());
    monitorData.setVersion(scRegistration.getMicroserviceInstance().getVersion());
    monitorData.setServiceId(scRegistration.getMicroserviceInstance().getBackendMicroservice().getServiceId());
    monitorData.setInstance(scRegistration.getMicroserviceInstance().getBackendMicroserviceInstance().getHostName());
    monitorData.setInstanceId(scRegistration.getMicroserviceInstance().getInstanceId());
    monitorData.setEnvironment(scRegistration.getMicroserviceInstance().getEnvironment());
  }

  @Override
  public void extractInterfaceInfo(MonitorData monitorData) {
    if (meters == null) {
      return;
    }
    PublishModelFactory factory = new PublishModelFactory(meters);
    DefaultPublishModel model = factory.createDefaultPublishModel();

    Map<String, InterfaceInfo> combinedResults = new HashMap<>();
    extractProviderInfo(model, combinedResults);
    extractConsumerInfo(model, combinedResults);
    extractEdgeInfo(model, combinedResults);
    combinedResults.forEach((k, v) -> {
      v.setFailureRate(v.getTotal() == 0 ? 0 : v.getFailure() / (double) v.getTotal());
      monitorData.addInterfaceInfo(v);
    });
  }

  private void extractProviderInfo(DefaultPublishModel model, Map<String, InterfaceInfo> combinedResults) {
    OperationPerfGroups producerPerf = model.getProducer().getOperationPerfGroups();
    if (producerPerf == null) {
      return;
    }

    for (Map<String, OperationPerfGroup> statusMap : producerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        for (int i = 0; i < perfGroup.getOperationPerfs().size(); i++) {
          OperationPerf operationPerf = perfGroup.getOperationPerfs().get(i);
          PerfInfo stageTotal = operationPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
          String name = NAME_PROVIDER + operationPerf.getOperation();
          InterfaceInfo interfaceInfo = combinedResults.computeIfAbsent(name,
              k -> {
                InterfaceInfo obj = new InterfaceInfo();
                obj.setName(name);
                return obj;
              });
          // dashboard calculates the latest 10 seconds, different with metrics cycle.
          interfaceInfo.setTotal(
              doubleToInt(interfaceInfo.getTotal() + 10 * stageTotal.getTps()));
          if (perfGroup.getStatus().matches(CODE_SUCCESS)) {
            interfaceInfo.setQps(stageTotal.getTps());
            interfaceInfo.setLatency(doubleToInt(stageTotal.calcMsLatency()));
          } else {
            interfaceInfo.setFailure(
                doubleToInt(interfaceInfo.getTotal() + stageTotal.getMsTotalTime() * stageTotal.getTps()));
            if (perfGroup.getStatus().equals(CODE_TIMEOUT)) {
              interfaceInfo.setCountTimeout(
                  doubleToInt(interfaceInfo.getCountTimeout() + stageTotal.getMsTotalTime() * stageTotal.getTps()));
            }
          }
        }
      }
    }
  }

  private void extractEdgeInfo(DefaultPublishModel model, Map<String, InterfaceInfo> combinedResults) {
    OperationPerfGroups edgePerf = model.getEdge().getOperationPerfGroups();
    if (edgePerf == null) {
      return;
    }
    for (Map<String, OperationPerfGroup> statusMap : edgePerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        for (int i = 0; i < perfGroup.getOperationPerfs().size(); i++) {
          OperationPerf operationPerf = perfGroup.getOperationPerfs().get(i);
          PerfInfo stageTotal = operationPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
          String name = NAME_CONSUMER + operationPerf.getOperation();
          InterfaceInfo interfaceInfo = combinedResults.computeIfAbsent(name,
              k -> {
                InterfaceInfo obj = new InterfaceInfo();
                obj.setName(name);
                return obj;
              });
          // dashboard calculates the latest 10 seconds, different with metrics cycle.
          interfaceInfo.setTotal(
              doubleToInt(interfaceInfo.getTotal() + 10 * stageTotal.getTps()));
          if (perfGroup.getStatus().matches(CODE_SUCCESS)) {
            interfaceInfo.setQps(stageTotal.getTps());
            interfaceInfo.setLatency(doubleToInt(stageTotal.calcMsLatency()));
          } else {
            interfaceInfo.setFailure(
                doubleToInt(interfaceInfo.getTotal() + stageTotal.getMsTotalTime() * stageTotal.getTps()));
            if (perfGroup.getStatus().equals(CODE_TIMEOUT)) {
              interfaceInfo.setCountTimeout(
                  doubleToInt(interfaceInfo.getCountTimeout() + stageTotal.getMsTotalTime() * stageTotal.getTps()));
            }
          }
        }
      }
    }
  }

  private void extractConsumerInfo(DefaultPublishModel model, Map<String, InterfaceInfo> combinedResults) {
    OperationPerfGroups consumerPerf = model.getConsumer().getOperationPerfGroups();
    if (consumerPerf == null) {
      return;
    }
    for (Map<String, OperationPerfGroup> statusMap : consumerPerf.getGroups().values()) {
      for (OperationPerfGroup perfGroup : statusMap.values()) {
        for (int i = 0; i < perfGroup.getOperationPerfs().size(); i++) {
          OperationPerf operationPerf = perfGroup.getOperationPerfs().get(i);
          PerfInfo stageTotal = operationPerf.findStage(MeterInvocationConst.STAGE_TOTAL);
          String name = NAME_CONSUMER + operationPerf.getOperation();
          InterfaceInfo interfaceInfo = combinedResults.computeIfAbsent(name,
              k -> {
                InterfaceInfo obj = new InterfaceInfo();
                obj.setName(name);
                return obj;
              });
          // dashboard calculates the latest 10 seconds, different with metrics cycle.
          interfaceInfo.setTotal(
              doubleToInt(interfaceInfo.getTotal() + 10 * stageTotal.getTps()));
          if (perfGroup.getStatus().matches(CODE_SUCCESS)) {
            interfaceInfo.setQps(stageTotal.getTps());
            interfaceInfo.setLatency(doubleToInt(stageTotal.calcMsLatency()));
          } else {
            interfaceInfo.setFailure(
                doubleToInt(interfaceInfo.getTotal() + stageTotal.getMsTotalTime() * stageTotal.getTps()));
            if (perfGroup.getStatus().equals(CODE_TIMEOUT)) {
              interfaceInfo.setCountTimeout(
                  doubleToInt(interfaceInfo.getCountTimeout() + stageTotal.getMsTotalTime() * stageTotal.getTps()));
            }
          }
        }
      }
    }
  }

  private int doubleToInt(Double d) {
    return d.intValue();
  }

  @Subscribe
  public void onPolledEvent(PolledEvent event) {
    this.meters = event.getMeters();
  }
}
