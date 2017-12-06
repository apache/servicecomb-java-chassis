/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.example.ewf;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.metrics.core.provider.MetricsPublisher;

//manage and init ServoObservers
@Component
public class EwfInitializer {

  private static final String METRICS_FILE_WRITE_INTERVAL = "servicecomb.metrics.example.ewf.write_millisecond";

  private static final String METRICS_FILE_ENABLED = "servicecomb.metrics.example.ewf.enabled";

  private final int writeInterval;

  private final MetricsFileOutput fileOutput;

  private final MetricsContentConvertor convertor;

  private final MetricsContentFormatter formatter;

  private final MetricsPublisher publisher;

  @Autowired
  public EwfInitializer(MetricsPublisher publisher, MetricsFileOutput fileOutput, MetricsContentConvertor convertor,
      MetricsContentFormatter formatter) {
    this.writeInterval = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_FILE_WRITE_INTERVAL, 5000).get();
    this.publisher = publisher;
    this.fileOutput = fileOutput;
    this.convertor = convertor;
    this.formatter = formatter;

    if (isRollingFileEnabled()) {
      final Runnable executor = this::doOutput;
      Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(executor, 0, writeInterval, MILLISECONDS);
    }
  }

  private boolean isRollingFileEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(METRICS_FILE_ENABLED, false).get();
  }

  private void doOutput() {
    //only filter instance level
    Map<String, Number> metrics = publisher.metricsFilterWithGroupAndLevel("servicecomb","instance");
    //first convert metrics to Map<String,String>
    Map<String, String> convertedMetrics = convertor.convert(metrics);
    //second format output content style
    Map<String, String> formattedMetrics = formatter.format(convertedMetrics);
    //finally output to file
    fileOutput.output(formattedMetrics);
  }
}
