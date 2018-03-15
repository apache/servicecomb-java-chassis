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

public interface TagFinder {
  static TagFinder build(Object obj) {
    if (String.class.isInstance(obj)) {
      return new DefaultTagFinder((String) obj);
    }

    if (TagFinder.class.isInstance(obj)) {
      return (TagFinder) obj;
    }

    throw new IllegalArgumentException(
        "only support String or TagFinder, but got " +
            (obj == null ? "null" : obj.getClass().getName()));
  }

  String getTagKey();

  // read target tag from tags
  // return directly or do some change and then return
  Tag find(Iterable<Tag> tags);
}
