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

import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultClientEndpointMetric;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultEndpointMetric;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class HttpClientEndpointMeter extends EndpointMeter {
  public static final String QUEUE_COUNT = "queueCount";

  private Id idQueueCount;

  public HttpClientEndpointMeter(Id id, DefaultEndpointMetric metric) {
    super(id, metric);
    idQueueCount = this.id.withTag(STATISTIC, QUEUE_COUNT);
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, double secondInterval) {
    super.calcMeasurements(measurements, msNow, secondInterval);

    long queueCount = ((DefaultClientEndpointMetric) metric).getQueueCount();
    measurements.add(newMeasurement(idQueueCount, msNow, queueCount));
  }
}
