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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestVersionRule {
  static class VersionRuleForTest extends VersionRule {
    public VersionRuleForTest(String versionRule) {
      super(versionRule);
    }

    @Override
    public boolean isMatch(Version version, Version latestVersion) {
      return true;
    }
  }

  VersionRule versionRule = new VersionRuleForTest("abc");

  @Test
  public void isAccept() {
    Assertions.assertTrue(versionRule.isAccept(null));
  }

  @Test
  public void getVersionRule() {
    Assertions.assertEquals("abc", versionRule.getVersionRule());
  }
}
