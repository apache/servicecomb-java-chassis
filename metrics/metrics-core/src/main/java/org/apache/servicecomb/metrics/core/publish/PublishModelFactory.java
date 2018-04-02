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
package org.apache.servicecomb.metrics.core.publish;

import java.util.List;

import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementGroupConfig;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.swagger.invocation.InvocationType;

import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.patterns.ThreadPoolMonitorPublishModelFactory;

public class PublishModelFactory {
  private MeasurementTree tree;

  public PublishModelFactory(List<Meter> meters) {
    tree = createMeasurementTree(meters);
  }

  protected MeasurementTree createMeasurementTree(List<Meter> meters) {
    MeasurementGroupConfig groupConfig = createMeasurementGroupConfig();

    MeasurementTree tree = new MeasurementTree();
    tree.from(meters.iterator(), groupConfig);
    return tree;
  }

  protected MeasurementGroupConfig createMeasurementGroupConfig() {
    MeasurementGroupConfig groupConfig = new MeasurementGroupConfig();
    groupConfig.addGroup(MeterInvocationConst.INVOCATION_NAME,
        MeterInvocationConst.TAG_ROLE,
        MeterInvocationConst.TAG_TRANSPORT,
        MeterInvocationConst.TAG_OPERATION,
        MeterInvocationConst.TAG_STATUS,
        MeterInvocationConst.TAG_STAGE,
        MeterInvocationConst.TAG_STATISTIC);

    return groupConfig;
  }

  protected OperationPerfGroups generateOperationPerfGroups(MeasurementTree tree, String invocationTypeName) {
    MeasurementNode node = tree.findChild(MeterInvocationConst.INVOCATION_NAME, invocationTypeName);
    if (node == null) {
      return null;
    }

    OperationPerfGroups groups = new OperationPerfGroups();

    // group by transport
    for (MeasurementNode transportNode : node.getChildren().values()) {
      // group by operation
      for (MeasurementNode operationNode : transportNode.getChildren().values()) {
        // group by status
        for (MeasurementNode statusNode : operationNode.getChildren().values()) {
          PublishUtils.addOperationPerfGroups(groups, transportNode.getName(), operationNode.getName(), statusNode);
        }
      }
    }

    return groups;
  }

  public DefaultPublishModel createDefaultPublishModel() {
    DefaultPublishModel model = new DefaultPublishModel();

    model.getConsumer()
        .setOperationPerfGroups(generateOperationPerfGroups(tree, InvocationType.CONSUMER.name()));
    model.getProducer()
        .setOperationPerfGroups(generateOperationPerfGroups(tree, InvocationType.PRODUCER.name()));

    ThreadPoolMonitorPublishModelFactory.create(tree, model.getThreadPools());

    return model;
  }
}
