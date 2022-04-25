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

package org.apache.servicecomb.registry.version;

import org.apache.servicecomb.foundation.common.Version;
import org.apache.servicecomb.registry.definition.DefinitionConst;

public class VersionRuleLatestParser implements VersionRuleParser {
  static class LatestVersionRule extends VersionRule {
    public LatestVersionRule(String versionRule) {
      super(versionRule);
    }

    @Override
    public boolean isAccept(Version version) {
      return true;
    }

    public boolean isMatch(Version version, Version latestVersion) {
      return version.equals(latestVersion);
    }
  }

  @Override
  public VersionRule parse(String strVersionRule) {
    if (!DefinitionConst.VERSION_RULE_LATEST.equals(strVersionRule)) {
      return null;
    }

    return new LatestVersionRule(strVersionRule);
  }
}
