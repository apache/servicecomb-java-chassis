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

package org.apache.servicecomb.metrics.overwatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;

import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class MetricsSender {
  private static final Logger logger = LoggerFactory.getLogger(MetricsSender.class);

  private static final String METRICS_OVERWATCH_ADDRESS = "servicecomb.metrics.overwatch.address";

  private static final String METRICS_OVERWATCH_WINDOW_TIME = "servicecomb.metrics.overwatch.window_time";

  private final String overwatchURL;

  private final DataSource dataSource;

  private final MetricsConvertor formatter;

  private final Long windowTime;

  private final RestTemplate template;

  private String serviceName;

  @Autowired
  public MetricsSender(DataSource dataSource, MetricsConvertor formatter) {
    this.dataSource = dataSource;
    this.formatter = formatter;
    this.overwatchURL = "http://" + DynamicPropertyFactory.getInstance()
        .getStringProperty(METRICS_OVERWATCH_ADDRESS, "localhost:3000").get() + "/stats/";

    long windowTime = DynamicPropertyFactory.getInstance().getLongProperty(METRICS_OVERWATCH_WINDOW_TIME, 0).get();
    if (!dataSource.getAppliedWindowTime().contains(windowTime)) {
      //if no config or illegal value, use datasource first window time
      windowTime = dataSource.getAppliedWindowTime().get(0);
      logger.error(
          METRICS_OVERWATCH_WINDOW_TIME + " no set or illegal value, use datasource first window time : " + windowTime);
    }
    this.windowTime = windowTime;
    this.template = new RestTemplate();
  }

  public void startSend() {
    serviceName = RegistryUtils.getMicroservice().getServiceName();
    final Runnable executor = this::sendMetrics;
    Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(executor, 0, windowTime, MILLISECONDS);
  }

  public void sendMetrics() {
    SystemStatus systemStatus = formatter.convert(this.serviceName, dataSource.getRegistryMetric(this.windowTime));

    ResponseEntity<String> result = this.template.postForEntity(this.overwatchURL, systemStatus, String.class);
  }
}
