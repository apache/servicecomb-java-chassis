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

// 1.0.0-2.0.0
public class VersionRuleRangeParser implements VersionRuleParser {
  class RangeVersionRule extends VersionRule {
    private final Version from;

    private final Version to;

    public RangeVersionRule(String versionRule, Version from, Version to) {
      super(versionRule);

      this.from = from;
      this.to = to;
    }

    public boolean isMatch(Version version, Version latestVersion) {
      return version.compareTo(from) >= 0 && version.compareTo(to) < 0;
    }
  }

  @Override
  public VersionRule parse(String strVersionRule) {
    int pos = strVersionRule.indexOf('-');
    if (pos <= 0 || pos == strVersionRule.length() - 1) {
      return null;
    }

    Version from = new Version(strVersionRule.substring(0, pos));
    Version to = new Version(strVersionRule.substring(pos + 1));
    return new RangeVersionRule(from.getVersion() + "-" + to.getVersion(), from, to);
  }
}
