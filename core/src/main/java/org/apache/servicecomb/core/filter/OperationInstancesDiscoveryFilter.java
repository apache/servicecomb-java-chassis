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

package org.apache.servicecomb.core.filter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.CoreMetaUtils;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.consumer.MicroserviceVersion;
import org.apache.servicecomb.registry.discovery.AbstractDiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.version.VersionRule;
import org.apache.servicecomb.registry.version.VersionRuleUtils;

import com.netflix.config.DynamicPropertyFactory;

/**
 * <pre>
 *   create operation related instances
 *
 *   Preconditions:
 *     compatible is ensure by appManager.getOrCreateMicroserviceVersionRule
 *     instances in "DiscoveryTreeNode parent" are compatible:
 *       new version can only add operations, not delete operations
 *
 *   eg:
 *     microservice name is ms1
 *     2 instances are 1.0.0, instance id are i1/i2, schemaId is s1, operations are o1/o2
 *     3 instances are 1.0.1, instance id are i3/i4/i5, schemaId is s1, operations are o1/o2/o3
 *
 *     will create nodes:
 *     {
 *       "ms1.s1.o1": {
 *         {"i1": instance-i1}, {"i2": instance-i2}, {"i3": instance-i3}, {"i4": instance-i4}, {"i5": instance-i5}
 *       },
 *       "ms1.s1.o2": {
 *         {"i1": instance-i1}, {"i2": instance-i2}, {"i3": instance-i3}, {"i4": instance-i4}, {"i5": instance-i5}
 *       },
 *       "ms1.s1.o3": {
 *         {"i3": instance-i3}, {"i4": instance-i4}, {"i5": instance-i5}
 *       },
 *     }
 *     ms1.s1.o1 and ms1.s1.o2 should share the same map instance
 *
 *     that means, if invoke o1 or o2, can use 5 instances, but if invoke o3, can only use 3 instances
 *     by this filter, we can make sure that new operations will not route to old instances
 * </pre>
 */
public class OperationInstancesDiscoveryFilter extends AbstractDiscoveryFilter {
  @Override
  public int getOrder() {
    return -20000;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.operation.enabled", true).get();
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
    Invocation invocation = context.getInputParameters();
    // sort versions
    List<MicroserviceVersion> microserviceVersions = CoreMetaUtils.getMicroserviceVersions(invocation)
        .getVersions().values().stream()
        .sorted(Comparator.comparing(MicroserviceVersion::getVersion))
        .collect(Collectors.toList());

    Map<String, DiscoveryTreeNode> operationNodes = new ConcurrentHashMapEx<>();
    for (MicroserviceVersion microserviceVersion : microserviceVersions) {
      DiscoveryTreeNode shareNode = null;

      MicroserviceMeta microserviceMeta = CoreMetaUtils.getMicroserviceMeta(microserviceVersion);
      for (OperationMeta operationMeta : microserviceMeta.getOperations()) {
        DiscoveryTreeNode node = operationNodes.get(operationMeta.getMicroserviceQualifiedName());
        if (node == null) {
          // not exist, use the share node
          if (shareNode == null) {
            Map<String, MicroserviceInstance> instanceMap = microserviceVersion.getInstances().stream()
                .collect(Collectors.toMap(MicroserviceInstance::getInstanceId, Function.identity()));
            shareNode = createOperationNode(parent, microserviceVersion);
            shareNode.data(instanceMap);
          }

          operationNodes.put(operationMeta.getMicroserviceQualifiedName(), shareNode);
          continue;
        }

        // exist, append instances
        microserviceVersion.getInstances().forEach(microserviceInstance ->
            node.mapData().put(microserviceInstance.getInstanceId(), microserviceInstance));
      }
    }

    parent.children(operationNodes);
  }

  private DiscoveryTreeNode createOperationNode(DiscoveryTreeNode parent, MicroserviceVersion microserviceVersion) {
    VersionRule versionRule = VersionRuleUtils.getOrCreate(microserviceVersion.getVersion().getVersion() + "+");
    return new DiscoveryTreeNode()
        .subName(parent, versionRule.getVersionRule())
        .data(new HashMap<>());
  }
}
