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

package org.apache.servicecomb.metrics.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.apache.servicecomb.metrics.core.MonitorManager;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

public class MetricsCollector extends Collector implements Collector.Describable {

  @Override
  public List<MetricFamilySamples> collect() {
    return load();
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return load();
  }

  private List<MetricFamilySamples> load() {
    Map<String, Double> metrics = MonitorManager.getInstance().measure();
    List<MetricFamilySamples> familySamples = new ArrayList<>();

    List<Sample> samples = new ArrayList<>();
    for (Entry<String, Double> metric : metrics.entrySet()) {
      List<String> tagNames = new ArrayList<>();
      List<String> tagValues = new ArrayList<>();
      String name = metric.getKey();
      if (metric.getKey().contains("(")) {
        String[] nameAndTag = metric.getKey().split("\\(");
        name = nameAndTag[0];
        String[] tagAnValues = nameAndTag[1].split("[=,)]");
        for (int i = 0; i < tagAnValues.length; i += 2) {
          //we need put operation name in metrics name,not a label
          if (MetricsConst.TAG_OPERATION.equals(tagAnValues[i])) {
            name = name + "." + tagAnValues[i + 1];
          } else if (!"type".equals(tagAnValues[i])) {
            tagNames.add(tagAnValues[i]);
            tagValues.add(tagAnValues[i + 1]);
          }
        }
      }
      samples.add(new Sample(formatMetricName(name), tagNames, tagValues, metric.getValue()));
    }
    familySamples.add(new MetricFamilySamples("ServiceComb Metrics", Type.UNTYPED, "ServiceComb Metrics", samples));
    return familySamples;
  }

  //convert name for match prometheus
  private String formatMetricName(String name) {
    return name.replace(".", "_");
  }
}
