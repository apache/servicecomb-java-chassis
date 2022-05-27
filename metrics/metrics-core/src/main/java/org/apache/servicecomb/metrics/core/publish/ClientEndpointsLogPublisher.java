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

import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.metrics.core.VertxMetersInitializer;
import org.apache.servicecomb.metrics.core.meter.vertx.EndpointMeter;
import org.apache.servicecomb.metrics.core.meter.vertx.HttpClientEndpointMeter;

public class ClientEndpointsLogPublisher extends AbstractMeasurementNodeLogPublisher {
  public ClientEndpointsLogPublisher(MeasurementTree tree, StringBuilder sb, String meterName) {
    super(tree, sb, VertxMetersInitializer.VERTX_ENDPOINTS, meterName);
  }

  @Override
  public void print(boolean printDetail) {
    if (!printDetail) {
      return;
    }
    appendLine(sb, "    client.endpoints:");
    appendLine(sb,
        "      connectCount disconnectCount queue         connections requests latency send(Bps) receive(Bps) remote");

    List<MeasurementNode> measurements = new ArrayList<>(measurementNode.getChildren().size());
    measurements.addAll(measurementNode.getChildren().values());
    measurements.sort(MeasurementNode::compareTo);
    for (MeasurementNode address : measurements) {
      appendLine(sb, "      %-12.0f %-15.0f %-13.0f %-11.0f %-8.0f %-7.0f %-9s %-12s %s",
          address.findChild(EndpointMeter.CONNECT_COUNT).summary(),
          address.findChild(EndpointMeter.DISCONNECT_COUNT).summary(),
          address.findChild(HttpClientEndpointMeter.QUEUE_COUNT).summary(),
          address.findChild(EndpointMeter.CONNECTIONS).summary(),
          address.findChild(EndpointMeter.REQUESTS).summary(),
          address.findChild(EndpointMeter.LATENCY).summary(),
          NetUtils.humanReadableBytes((long) address.findChild(EndpointMeter.BYTES_WRITTEN).summary()),
          NetUtils.humanReadableBytes((long) address.findChild(EndpointMeter.BYTES_READ).summary()),
          address.getName()
      );
    }
  }
}
