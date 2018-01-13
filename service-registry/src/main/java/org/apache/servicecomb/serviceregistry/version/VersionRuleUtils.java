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

package org.apache.servicecomb.serviceregistry.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public final class VersionRuleUtils {
  private static List<VersionRuleParser> parsers = new ArrayList<>();

  private static Map<String, VersionRule> versionRuleCache = new ConcurrentHashMapEx<>();

  static {
    parsers.add(new VersionRuleLatestParser());
    parsers.add(new VersionRuleStartFromParser());
    parsers.add(new VersionRuleRangeParser());
    parsers.add(new VersionRuleFixedParser());
  }

  //1.0
  //1.0.0+
  //1.0.0-2.0.0
  //latest
  public static VersionRule getOrCreate(String strVersionRule) {
    Objects.requireNonNull(strVersionRule);

    return versionRuleCache.computeIfAbsent(strVersionRule, VersionRuleUtils::create);
  }

  public static VersionRule create(String strVersionRule) {
    strVersionRule = strVersionRule.trim();
    for (VersionRuleParser parser : parsers) {
      VersionRule versionRule = parser.parse(strVersionRule);
      if (versionRule != null) {
        return versionRule;
      }
    }

    throw new IllegalStateException("never run to here");
  }
}
