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

package io.servicecomb.metrics.extension.writefile;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.metrics.common.RegistryMetric;
import io.servicecomb.metrics.core.publish.DataSource;
import io.servicecomb.metrics.extension.writefile.config.MetricsFileWriter;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class WriteFileInitializer {

  private static final String METRICS_WINDOW_TIME = "servicecomb.metrics.window_time";

  private final int metricPoll;

  private final FileContentConvertor convertor;

  private final FileContentFormatter formatter;

  private final DataSource dataSource;

  private final MetricsFileWriter fileWriter;

  private final String filePrefix;

  @Autowired
  public WriteFileInitializer(MetricsFileWriter fileWriter, FileContentConvertor convertor,
      FileContentFormatter formatter, DataSource dataSource) {
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_WINDOW_TIME, 5000).get();
    this.fileWriter = fileWriter;
    this.convertor = convertor;
    this.formatter = formatter;
    this.dataSource = dataSource;

    //may any problem ?
    if (RegistryUtils.getServiceRegistry() == null) {
      RegistryUtils.init();
    }
    Microservice microservice = RegistryUtils.getMicroservice();
    this.filePrefix = microservice.getAppId() + "." + microservice.getServiceName() + ".";

    this.init();
  }

  public WriteFileInitializer(MetricsFileWriter fileWriter, FileContentConvertor convertor,
      FileContentFormatter formatter, DataSource dataSource, String filePrefix) {
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_WINDOW_TIME, 5000).get();
    this.fileWriter = fileWriter;
    this.convertor = convertor;
    this.formatter = formatter;
    this.dataSource = dataSource;
    this.filePrefix = filePrefix;
  }

  private void init() {
    final Runnable poller = this::run;
    Executors.newScheduledThreadPool(1)
        .scheduleWithFixedDelay(poller, 0, metricPoll, MILLISECONDS);
  }

  public void run() {
    RegistryMetric registryMetric = dataSource.getRegistryMetric();
    Map<String, String> convertedMetrics = convertor.convert(registryMetric);
    Map<String, String> formattedMetrics = formatter.format(convertedMetrics);

    for (String metricName : formattedMetrics.keySet()) {
      fileWriter.write(metricName, filePrefix, formattedMetrics.get(metricName));
    }
  }
}
