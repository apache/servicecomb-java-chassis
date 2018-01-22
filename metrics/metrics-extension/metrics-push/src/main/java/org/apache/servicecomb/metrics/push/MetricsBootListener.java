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

package org.apache.servicecomb.metrics.push;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricsBootListener implements BootListener {
  private static final Logger logger = LoggerFactory.getLogger(MetricsBootListener.class);

  private final MetricsPusher pusher;

  private final DataSource dataSource;

  private final long windowTime;

  @Autowired
  public MetricsBootListener(MetricsPusher pusher, DataSource dataSource) {
    this.pusher = pusher;
    this.dataSource = dataSource;
    long windowTime = pusher.getWindowTime();
    if (windowTime <= 0 || !dataSource.getAppliedWindowTime().contains(windowTime)) {
      this.windowTime = dataSource.getAppliedWindowTime().get(0);
      logger.error("window time no set or illegal value, use datasource first window time : " + this.windowTime);
    } else {
      this.windowTime = windowTime;
    }
  }

  @Override
  public void onBootEvent(BootEvent event) {
    if (EventType.BEFORE_REGISTRY.equals(event.getEventType())) {
      final Runnable executor = this::push;
      Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(executor, 0, windowTime, MILLISECONDS);
    }
  }

  private void push() {
    pusher.push(dataSource.getRegistryMetric(windowTime));
  }
}