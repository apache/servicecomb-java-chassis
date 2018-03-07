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

import com.netflix.spectator.api.Tag;

public class DefaultTagFinder implements TagFinder {
  private String tagKey;

  public DefaultTagFinder(String tagKey) {
    this.tagKey = tagKey;
  }

  @Override
  public String getTagKey() {
    return tagKey;
  }

  @Override
  public Tag find(Iterable<Tag> tags) {
    for (Tag tag : tags) {
      if (tag.key().equals(tagKey)) {
        return tag;
      }
    }

    return null;
  }
}
