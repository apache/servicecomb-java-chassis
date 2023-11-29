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
package org.apache.servicecomb.foundation.metrics.publish.spectator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

// like select * from meters group by ......
// but output a tree not a table
public class MeasurementTree extends MeasurementNode {
  public static final String TAG_LATENCY_DISTRIBUTION = "latencyDistribution";

  public static final String TAG_TYPE = "type";

  public MeasurementTree() {
    super(null, null, null);
  }

  // groupConfig:
  //   key: id name
  //   value: id tag keys
  // only id name exists in groupConfig will accept, others will be ignored
  public void from(Iterator<Meter> meters, MeasurementGroupConfig groupConfig) {
    meters.forEachRemaining(meter -> {
      Iterable<Measurement> measurements = meter.measure();
      from(meter.getId(), measurements, groupConfig);

      // This code snip is not very good design. But timer is quite special.
      if (meter instanceof Timer) {
        HistogramSnapshot snapshot = ((Timer) meter).takeSnapshot();
        CountAtBucket[] countAtBuckets = snapshot.histogramCounts();
        if (countAtBuckets.length > 2) {
          List<Measurement> latency = new ArrayList<>(countAtBuckets.length);
          for (int i = 0; i < countAtBuckets.length; i++) {
            final int index = i;
            if (index == 0) {
              latency.add(new Measurement(() -> countAtBuckets[index].count(),
                  Statistic.VALUE));
              continue;
            }
            latency.add(new Measurement(() -> countAtBuckets[index].count() - countAtBuckets[index - 1].count(),
                Statistic.VALUE));
          }
          from(meter.getId().withTag(Tag.of(TAG_TYPE, TAG_LATENCY_DISTRIBUTION)), latency, groupConfig);
        }
      }
    });
  }

  public void from(Id id, Iterable<Measurement> measurements, MeasurementGroupConfig groupConfig) {
    for (Measurement measurement : measurements) {
      MeasurementNode node = addChild(id.getName(), id, measurement);

      List<TagFinder> tagFinders = groupConfig.findTagFinders(id.getName());
      if (tagFinders == null) {
        continue;
      }

      for (TagFinder tagFinder : tagFinders) {
        Tag tag = tagFinder.find(id.getTags());
        if (tag == null) {
          if (tagFinder.skipOnNull()) {
            break;
          }
          throw new IllegalStateException(
              String.format("tag key \"%s\" not exist in %s",
                  tagFinder.getTagKey(),
                  id));
        }

        node = node.addChild(tag.getValue(), id, measurement);
      }

      node.addChild(measurement.getStatistic().name(), id, measurement);
    }
  }
}
