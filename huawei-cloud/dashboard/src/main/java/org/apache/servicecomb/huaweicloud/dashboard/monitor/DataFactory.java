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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.huaweicloud.dashboard.monitor.data.MonitorConstant;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataProvider;
import org.apache.servicecomb.huaweicloud.dashboard.monitor.model.MonitorDataPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.netty.util.concurrent.DefaultThreadFactory;

public class DataFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataFactory.class);

  private static final int CORE_SIZE = 1;

  private boolean hasStart = false;

  @Autowired
  private List<MonitorDataProvider> dataProviders;

  @Autowired
  private MonitorDataPublisher publisher;

  @Autowired
  private MonitorConstant monitorConstant;

  private ScheduledExecutorService executorService = null;


  public DataFactory() {
    ThreadFactory threadFactory = new DefaultThreadFactory("monitor-datafactory");
    executorService = Executors.newScheduledThreadPool(CORE_SIZE, threadFactory);
  }

  public void setMonitorDataProviders(List<MonitorDataProvider> dataProviders) {
    this.dataProviders = dataProviders;
  }

  public void setMonitorDataPublisher(MonitorDataPublisher publisher) {
    this.publisher = publisher;
  }

  void start() {
    if (!hasStart) {
      publisher.init();

      StringBuilder sb = new StringBuilder();
      sb.append("Monitor data sender started. Configured data providers is {");
      for (MonitorDataProvider provider : dataProviders) {
        sb.append(provider.getClass().getName());
        sb.append(",");
      }
      sb.append("}");
      LOGGER.info(sb.toString());

      executorService.scheduleWithFixedDelay(() -> {
        try {
          sendData();
        } catch (Throwable e) {
          LOGGER.error("send monitor data error.", e);
        }
      }, monitorConstant.getInterval(), monitorConstant.getInterval(), TimeUnit.MILLISECONDS);
      hasStart = true;
    }
  }

  void sendData() {
    for (MonitorDataProvider provider : this.dataProviders) {
      if (provider.enabled()) {
        this.publisher.publish(provider);
      }
    }
  }
}
