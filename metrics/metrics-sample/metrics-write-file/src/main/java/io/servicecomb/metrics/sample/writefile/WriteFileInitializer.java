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

package io.servicecomb.metrics.sample.writefile;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.metrics.common.RegistryMetric;
import io.servicecomb.metrics.core.publish.DataSource;

@Component
public class WriteFileInitializer {

  private static final String METRICS_WINDOW_TIME = "servicecomb.metrics.window_time";

  private final int metricPoll;

  private final FileWriter fileOutput;

  private final FileContentConvertor convertor;

  private final FileContentFormatter formatter;

  private final DataSource dataSource;

  @Autowired
  public WriteFileInitializer(FileWriter fileOutput, FileContentConvertor convertor,
      FileContentFormatter formatter, DataSource dataSource) {
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_WINDOW_TIME, 5000).get();
    this.fileOutput = fileOutput;
    this.convertor = convertor;
    this.formatter = formatter;
    this.dataSource = dataSource;
    this.init();
  }

  public void init() {
    final Runnable poller = this::run;
    Executors.newScheduledThreadPool(1)
        .scheduleWithFixedDelay(poller, 0, metricPoll, MILLISECONDS);
  }

  private void run() {
    RegistryMetric registryMetric = dataSource.getRegistryMetric(0);
    Map<String, String> convertedMetrics = convertor.convert(registryMetric);
    Map<String, String> formattedMetrics = formatter.format(convertedMetrics);
    fileOutput.output(formattedMetrics);
  }
}
