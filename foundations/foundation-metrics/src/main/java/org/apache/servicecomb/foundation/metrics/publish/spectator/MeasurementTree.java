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

import java.util.Iterator;
import java.util.List;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Tag;

// like select * from meters group by ......
// but output a tree not a table
public class MeasurementTree extends MeasurementNode {
  public MeasurementTree() {
    super(null, null);
  }

  // groupConfig:
  //   key: id name
  //   value: id tag keys
  // only id name exists in groupConfig will accept, others will be ignored
  public void from(Iterator<Meter> meters, MeasurementGroupConfig groupConfig) {
    meters.forEachRemaining(meter -> {
      Iterable<Measurement> measurements = meter.measure();
      from(measurements, groupConfig);
    });
  }

  public void from(Iterable<Measurement> measurements, MeasurementGroupConfig groupConfig) {
    for (Measurement measurement : measurements) {
      Id id = measurement.id();
      MeasurementNode node = addChild(id.name(), measurement);

      List<TagFinder> tagFinders = groupConfig.findTagFinders(id.name());
      if (tagFinders == null) {
        continue;
      }

      for (TagFinder tagFinder : tagFinders) {
        Tag tag = tagFinder.find(id.tags());
        if (tag == null) {
          throw new IllegalStateException(
              String.format("tag key \"%s\" not exist in %s",
                  tagFinder.getTagKey(),
                  measurement));
        }

        node = node.addChild(tag.value(), measurement);
      }
    }
  }
}
