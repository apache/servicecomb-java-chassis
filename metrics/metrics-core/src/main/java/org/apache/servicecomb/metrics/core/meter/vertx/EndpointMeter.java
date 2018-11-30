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
package org.apache.servicecomb.metrics.core.meter.vertx;

import java.util.List;

import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class EndpointMeter {
  public static final String ADDRESS = "address";

  public static final String STATISTIC = "statistic";

  public static final String CONNECT_COUNT = "connectCount";

  public static final String DISCONNECT_COUNT = "disconnectCount";

  public static final String CONNECTIONS = "connections";

  public static final String BYTES_READ = "bytesRead";

  public static final String BYTES_WRITTEN = "bytesWritten";

  protected Id id;

  private Id idConnect;

  private Id idDisconnect;

  private Id idConnections;

  private Id idBytesRead;

  private Id idBytesWritten;

  protected DefaultEndpointMetric metric;

  private long lastConnectCount;

  private long lastDisconnectCount;

  private long lastBytesRead;

  private long lastBytesWritten;

  public EndpointMeter(Id id, DefaultEndpointMetric metric) {
    id = id.withTag(ADDRESS, metric.getAddress().toString());
    this.id = id;
    idConnect = id.withTag(STATISTIC, CONNECT_COUNT);
    idDisconnect = id.withTag(STATISTIC, DISCONNECT_COUNT);
    idConnections = id.withTag(STATISTIC, CONNECTIONS);
    idBytesRead = id.withTag(STATISTIC, BYTES_READ);
    idBytesWritten = id.withTag(STATISTIC, BYTES_WRITTEN);
    this.metric = metric;
  }

  public DefaultEndpointMetric getMetric() {
    return metric;
  }

  protected Measurement newMeasurement(Id id, long timestamp, Number n) {
    return new Measurement(id, timestamp, n.doubleValue());
  }

  public void calcMeasurements(List<Measurement> measurements, long msNow, double secondInterval) {
    long connectCount = metric.getConnectCount();
    long disconnectCount = metric.getDisconnectCount();
    long bytesRead = metric.getBytesRead();
    long bytesWritten = metric.getBytesWritten();

    measurements.add(newMeasurement(idConnect, msNow, connectCount - lastConnectCount));
    measurements.add(newMeasurement(idDisconnect, msNow, disconnectCount - lastDisconnectCount));
    measurements.add(newMeasurement(idConnections, msNow, connectCount - disconnectCount));
    measurements.add(newMeasurement(idBytesRead, msNow, (bytesRead - lastBytesRead) / secondInterval));
    measurements.add(newMeasurement(idBytesWritten, msNow, (bytesWritten - lastBytesWritten) / secondInterval));

    this.lastConnectCount = connectCount;
    this.lastDisconnectCount = disconnectCount;
    this.lastBytesRead = bytesRead;
    this.lastBytesWritten = bytesWritten;
  }
}
