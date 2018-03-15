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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.spectator.api.BasicTag;
import com.netflix.spectator.api.Tag;

public class TestDefaultTagFinder {
  TagFinder finder = new DefaultTagFinder("key");

  @Test
  public void getTagKey() {
    Assert.assertEquals("key", finder.getTagKey());
  }

  @Test
  public void readSucc() {
    Tag tag = new BasicTag("key", "value");
    List<Tag> tags = Arrays.asList(new BasicTag("t1", "t1v"),
        tag);

    Assert.assertSame(tag, finder.find(tags));
  }

  @Test
  public void readFail() {
    List<Tag> tags = Arrays.asList(new BasicTag("t1", "t1v"));

    Assert.assertNull(finder.find(tags));
  }
}
