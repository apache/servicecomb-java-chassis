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

import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import mockit.Deencapsulation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMeasurementGroupConfig {
  MeasurementGroupConfig config = new MeasurementGroupConfig();

  Map<String, List<TagFinder>> groups = Deencapsulation.getField(config, "groups");

  @Test
  public void defaultConstruct() {
    Assertions.assertTrue(groups.isEmpty());
  }

  @Test
  public void constructAddGroup() {
    config = new MeasurementGroupConfig("id", "tag1");
    groups = Deencapsulation.getField(config, "groups");

    MatcherAssert.assertThat(groups.keySet(), Matchers.contains("id"));
    MatcherAssert.assertThat(groups.get("id").stream().map(TagFinder::getTagKey).toArray(),
            Matchers.arrayContaining("tag1"));
  }

  @Test
  public void addGroup() {
    config.addGroup("id1", "tag1.1", "tag1.2");
    config.addGroup("id2", "tag2.1", "tag2.2");

    MatcherAssert.assertThat(groups.keySet(), Matchers.contains("id2", "id1"));
    MatcherAssert.assertThat(groups.get("id1").stream().map(TagFinder::getTagKey).toArray(), Matchers.arrayContaining("tag1.1", "tag1.2"));
    MatcherAssert.assertThat(groups.get("id2").stream().map(TagFinder::getTagKey).toArray(), Matchers.arrayContaining("tag2.1", "tag2.2"));
  }

  @Test
  public void findTagReaders() {
    config.addGroup("id1", "tag1.1", "tag1.2");
    config.addGroup("id2", "tag2.1", "tag2.2");

    MatcherAssert.assertThat(config.findTagFinders("id2").stream().map(TagFinder::getTagKey).toArray(),
            Matchers.arrayContaining("tag2.1", "tag2.2"));
  }
}
