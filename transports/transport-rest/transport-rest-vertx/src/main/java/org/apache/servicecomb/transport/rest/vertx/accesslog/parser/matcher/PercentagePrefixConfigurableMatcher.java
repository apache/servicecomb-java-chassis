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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

/**
 * for those access log item whose placeholder like "%{configpart}C"
 */
public class PercentagePrefixConfigurableMatcher implements AccessLogItemMatcher {

  public static final String GENERAL_PREFIX = "%{";

  private static final Map<String, AccessLogItemTypeEnum> SUFFIX_PLACEHOLDER_ENUM_MAP = new LinkedHashMap<>();

  public static final String SUFFIX_HEAD = "}";

  static {
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}t", AccessLogItemTypeEnum.DATETIME_CONFIGURABLE);
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}i", AccessLogItemTypeEnum.REQUEST_HEADER);
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}o", AccessLogItemTypeEnum.RESPONSE_HEADER);
    SUFFIX_PLACEHOLDER_ENUM_MAP.put("}C", AccessLogItemTypeEnum.COOKIE);
  }

  @Override
  public AccessLogItemLocation match(String rawPattern, int offset) {
    int begin = rawPattern.indexOf(GENERAL_PREFIX, offset);
    if (begin < 0) {
      return null;
    }

    int end = rawPattern.indexOf(SUFFIX_HEAD, begin);
    if (end < 0) {
      return null;
    }

    for (Entry<String, AccessLogItemTypeEnum> entry : SUFFIX_PLACEHOLDER_ENUM_MAP.entrySet()) {
      if (rawPattern.startsWith(entry.getKey(), end)) {
        return new AccessLogItemLocation().setStart(begin).setEnd(end + entry.getKey().length())
            .setPlaceHolder(entry.getValue());
      }
    }

    return null;
  }
}
