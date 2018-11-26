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

import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.spectator.MeasurementTree;
import org.apache.servicecomb.metrics.core.VertxMetersInitializer;
import org.apache.servicecomb.metrics.core.meter.vertx.EndpointMeter;
import org.apache.servicecomb.metrics.core.meter.vertx.ServerEndpointMeter;

public class ServerEndpointsLogPublisher extends AbstractMeasurementNodeLogPublisher {
  public ServerEndpointsLogPublisher(MeasurementTree tree, StringBuilder sb, String meterName) {
    super(tree, sb, VertxMetersInitializer.VERTX_ENDPOINTS, meterName);
  }

  @Override
  public void print(boolean printDetail) {
    appendLine(sb, "    server.endpoints:");
    appendLine(sb,
        "      listen                connectCount    disconnectCount rejectByLimit   connections  send(B)      receive(B)");

    double connect = 0;
    double disconnect = 0;
    double reject = 0;
    double connections = 0;
    double readSize = 0;
    double writeSize = 0;
    for (MeasurementNode address : measurementNode.getChildren().values()) {
      connect += address.findChild(EndpointMeter.CONNECT_COUNT).summary();
      disconnect += address.findChild(EndpointMeter.DISCONNECT_COUNT).summary();
      reject += address.findChild(ServerEndpointMeter.REJECT_BY_CONNECTION_LIMIT).summary();
      connections += address.findChild(EndpointMeter.CONNECTIONS).summary();
      readSize += address.findChild(EndpointMeter.BYTES_READ).summary();
      writeSize += address.findChild(EndpointMeter.BYTES_WRITTEN).summary();

      if (printDetail) {
        appendLine(sb, "      %-21s %-15.0f %-15.0f %-15.0f %-12.0f %-12s %-12s",
            address.getName(),
            address.findChild(EndpointMeter.CONNECT_COUNT).summary(),
            address.findChild(EndpointMeter.DISCONNECT_COUNT).summary(),
            address.findChild(ServerEndpointMeter.REJECT_BY_CONNECTION_LIMIT).summary(),
            address.findChild(EndpointMeter.CONNECTIONS).summary(),
            NetUtils.humanReadableBytes((long) address.findChild(EndpointMeter.BYTES_WRITTEN).summary()),
            NetUtils.humanReadableBytes((long) address.findChild(EndpointMeter.BYTES_READ).summary())
        );
      }
    }

    appendLine(sb, "      %-21s %-15.0f %-15.0f %-15.0f %-12.0f %-12s %-12s",
        "(summary)",
        connect,
        disconnect,
        reject,
        connections,
        NetUtils.humanReadableBytes((long) writeSize),
        NetUtils.humanReadableBytes((long) readSize)
    );
  }
}
