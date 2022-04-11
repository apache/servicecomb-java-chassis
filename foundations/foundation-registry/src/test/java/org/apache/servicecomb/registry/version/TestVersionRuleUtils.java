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

import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.registry.version.VersionRuleFixedParser.FixedVersionRule;
import org.apache.servicecomb.registry.version.VersionRuleLatestParser.LatestVersionRule;
import org.apache.servicecomb.registry.version.VersionRuleRangeParser.RangeVersionRule;
import org.apache.servicecomb.registry.version.VersionRuleStartFromParser.StartFromVersionRule;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestVersionRuleUtils {

  @Test
  public void fixed() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate("1");
    MatcherAssert.assertThat(versionRule, Matchers.instanceOf(FixedVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate("1"));
  }

  @Test
  public void latest() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate(DefinitionConst.VERSION_RULE_LATEST);
    MatcherAssert.assertThat(versionRule, Matchers.instanceOf(LatestVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate(DefinitionConst.VERSION_RULE_LATEST));
  }

  @Test
  public void range() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate("1-2");
    MatcherAssert.assertThat(versionRule, Matchers.instanceOf(RangeVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate("1-2"));
  }

  @Test
  public void startFrom() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate("1+");
    MatcherAssert.assertThat(versionRule, Matchers.instanceOf(StartFromVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate("1+"));
  }

  @Test
  public void invalid() {
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
      VersionRuleUtils.getOrCreate("");
    });
    Assertions.assertEquals("Invalid major \"\", version \"\".", exception.getMessage());
  }
}
