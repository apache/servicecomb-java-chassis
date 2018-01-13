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

import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.version.VersionRuleFixedParser.FixedVersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleLatestParser.LatestVersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleRangeParser.RangeVersionRule;
import org.apache.servicecomb.serviceregistry.version.VersionRuleStartFromParser.StartFromVersionRule;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestVersionRuleUtils {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void fixed() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate("1");
    Assert.assertThat(versionRule, Matchers.instanceOf(FixedVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate("1"));
  }

  @Test
  public void latest() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate(DefinitionConst.VERSION_RULE_LATEST);
    Assert.assertThat(versionRule, Matchers.instanceOf(LatestVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate(DefinitionConst.VERSION_RULE_LATEST));
  }

  @Test
  public void range() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate("1-2");
    Assert.assertThat(versionRule, Matchers.instanceOf(RangeVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate("1-2"));
  }

  @Test
  public void startFrom() {
    VersionRule versionRule = VersionRuleUtils.getOrCreate("1+");
    Assert.assertThat(versionRule, Matchers.instanceOf(StartFromVersionRule.class));
    Assert.assertSame(versionRule, VersionRuleUtils.getOrCreate("1+"));
  }

  @Test
  public void invalid() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Invalid major \"\", version \"\"."));

    VersionRuleUtils.getOrCreate("");
  }
}
