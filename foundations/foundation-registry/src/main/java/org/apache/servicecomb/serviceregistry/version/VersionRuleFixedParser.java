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

// fixed, this should be the last parser
// will not check version format
public class VersionRuleFixedParser implements VersionRuleParser {
  class FixedVersionRule extends VersionRule {
    private final Version version;

    public FixedVersionRule(String versionRule, Version version) {
      super(versionRule);

      this.version = version;
    }

    public boolean isMatch(Version version, Version latestVersion) {
      return this.version.equals(version);
    }
  }

  @Override
  public VersionRule parse(String strVersionRule) {
    Version version = new Version(strVersionRule);
    return new FixedVersionRule(version.getVersion(), version);
  }
}
