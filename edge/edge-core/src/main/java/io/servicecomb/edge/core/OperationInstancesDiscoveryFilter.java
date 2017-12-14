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

package io.servicecomb.edge.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceVersionMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.consumer.AppManager;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersion;
import io.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import io.servicecomb.serviceregistry.discovery.AbstractDiscoveryFilter;
import io.servicecomb.serviceregistry.discovery.DiscoveryContext;
import io.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import io.servicecomb.serviceregistry.version.VersionRule;
import io.servicecomb.serviceregistry.version.VersionRuleUtils;

public class OperationInstancesDiscoveryFilter extends AbstractDiscoveryFilter {
  private final static String VERSION_RULE = "versionRule";

  @Override
  public int getOrder() {
    return -10000;
  }

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    Invocation invocation = context.getInputParameters();
    return invocation.getMicroserviceQualifiedName();
  }

  @Override
  public void init(DiscoveryContext context, DiscoveryTreeNode parent) {
    Map<MicroserviceVersionMeta, Map<String, MicroserviceInstance>> versionMap =
        groupByVersion(context.getInputParameters(), parent.data());
    Map<String, DiscoveryTreeNode> operationNodes = initOperationNodes(parent, versionMap);
    fillInstances(operationNodes, versionMap);

    parent.children(operationNodes);
  }

  protected void fillInstances(Map<String, DiscoveryTreeNode> operationNodes,
      Map<MicroserviceVersionMeta, Map<String, MicroserviceInstance>> versionMap) {
    for (Entry<MicroserviceVersionMeta, Map<String, MicroserviceInstance>> entry : versionMap.entrySet()) {
      for (DiscoveryTreeNode node : operationNodes.values()) {
        // versionRule is startFrom logic, so isAccept is enough
        VersionRule versionRule = node.attribute(VERSION_RULE);
        if (versionRule.isAccept(entry.getKey().getVersion())) {
          node.mapData().putAll(entry.getValue());
        }
      }
    }
  }

  protected Map<String, DiscoveryTreeNode> initOperationNodes(DiscoveryTreeNode parent,
      Map<MicroserviceVersionMeta, Map<String, MicroserviceInstance>> versionMap) {
    Map<String, DiscoveryTreeNode> tmpChildren = new ConcurrentHashMap<>();
    versionMap
        .keySet()
        .stream()
        .sorted(Comparator.comparing(MicroserviceVersion::getVersion))
        .forEach(meta -> {
          for (OperationMeta operationMeta : meta.getMicroserviceMeta().getOperations()) {
            tmpChildren.computeIfAbsent(operationMeta.getMicroserviceQualifiedName(), qualifiedName -> {
              VersionRule versionRule = VersionRuleUtils.getOrCreate(meta.getVersion().getVersion() + "+");
              return new DiscoveryTreeNode()
                  .attribute(VERSION_RULE, versionRule)
                  .subName(parent, versionRule.getVersionRule())
                  .data(new HashMap<>());
            });
          }
        });
    return tmpChildren;
  }

  protected Map<MicroserviceVersionMeta, Map<String, MicroserviceInstance>> groupByVersion(Invocation invocation,
      Map<String, MicroserviceInstance> instances) {
    OperationMeta latestOperationMeta = invocation.getOperationMeta();
    MicroserviceMeta latestMicroserviceMeta = latestOperationMeta.getSchemaMeta().getMicroserviceMeta();
    AppManager appManager = RegistryUtils.getServiceRegistry().getAppManager();
    MicroserviceVersions MicroserviceVersions =
        appManager.getOrCreateMicroserviceVersions(latestMicroserviceMeta.getAppId(), latestMicroserviceMeta.getName());

    Map<MicroserviceVersionMeta, Map<String, MicroserviceInstance>> versionMap = new IdentityHashMap<>();
    for (MicroserviceInstance instance : instances.values()) {
      MicroserviceVersionMeta versionMeta = MicroserviceVersions.getVersion(instance.getServiceId());
      Map<String, MicroserviceInstance> versionInstances = versionMap.computeIfAbsent(versionMeta, vm -> {
        return new HashMap<>();
      });
      versionInstances.put(instance.getInstanceId(), instance);
    }
    return versionMap;
  }
}
