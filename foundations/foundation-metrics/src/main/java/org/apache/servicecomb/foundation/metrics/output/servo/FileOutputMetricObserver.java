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

package org.apache.servicecomb.foundation.metrics.output.servo;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.metrics.output.MetricsFileOutput;

import com.netflix.servo.Metric;
import com.netflix.servo.publish.BaseMetricObserver;
import com.netflix.servo.util.Preconditions;

public class FileOutputMetricObserver extends BaseMetricObserver {
  private final MetricsFileOutput metricsOutput;

  private final MetricsContentConvertor convertor;

  private final MetricsContentFormatter formatter;

  public FileOutputMetricObserver(MetricsFileOutput metricsOutput,
      MetricsContentConvertor convertor, MetricsContentFormatter formatter) {
    super("fileOutputObserver");
    this.metricsOutput = metricsOutput;
    this.convertor = convertor;
    this.formatter = formatter;
  }

  @Override
  public void updateImpl(List<Metric> metrics) {
    Preconditions.checkNotNull(metrics, "metrics");
    //first convert metrics to Map<String,String>
    Map<String, String> convertedMetrics = convertor.convert(metrics);
    //second format output content style
    Map<String, String> formattedMetrics = formatter.format(convertedMetrics);
    //finally output to file
    metricsOutput.output(formattedMetrics);
  }
}


