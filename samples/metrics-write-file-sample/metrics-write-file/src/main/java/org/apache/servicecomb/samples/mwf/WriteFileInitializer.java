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

package org.apache.servicecomb.samples.mwf;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.MetricsConfig;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class WriteFileInitializer {
  private final int metricPoll;

  private FileContentConvertor convertor;

  private FileContentFormatter formatter;

  private final DataSource dataSource;

  private final MetricsFileWriter fileWriter;

  private String filePrefix;

  private String hostName;

  @Autowired
  public WriteFileInitializer(MetricsFileWriter fileWriter, DataSource dataSource) {
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(MetricsConfig.METRICS_POLLING_TIME, 5000).get();
    this.fileWriter = fileWriter;
    this.dataSource = dataSource;
    this.convertor = new SimpleFileContentConvertor();
  }

  public WriteFileInitializer(MetricsFileWriter fileWriter, DataSource dataSource, String hostName, String filePrefix) {
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(MetricsConfig.METRICS_POLLING_TIME, 5000).get();
    this.fileWriter = fileWriter;
    this.dataSource = dataSource;
    this.hostName = hostName;
    this.filePrefix = filePrefix;
    this.convertor = new SimpleFileContentConvertor();
    this.formatter = new SimpleFileContentFormatter(hostName, filePrefix);
  }

  public void startOutput() {
    if (StringUtils.isEmpty(filePrefix)) {
      Microservice microservice = RegistryUtils.getMicroservice();
      filePrefix = microservice.getAppId() + "." + microservice.getServiceName();
    }
    if (StringUtils.isEmpty(hostName)) {
      hostName = NetUtils.getHostName();
      if (StringUtils.isEmpty(hostName)) {
        hostName = NetUtils.getHostAddress();
      }
    }

    formatter = new SimpleFileContentFormatter(hostName, filePrefix);

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
