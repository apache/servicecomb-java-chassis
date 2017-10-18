/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.version;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.serviceregistry.definition.DefinitionConst;

public class TestVersionRuleLatestParser {
  VersionRuleParser parser = new VersionRuleLatestParser();

  VersionRule versionRule = parser.parse(DefinitionConst.VERSION_RULE_LATEST);

  @Test
  public void parseInvalue() {
    Assert.assertNull(parser.parse(""));
  }

  @Test
  public void parseNormal() {
    Assert.assertEquals(DefinitionConst.VERSION_RULE_LATEST, versionRule.getVersionRule());
  }

  @Test
  public void isAccept() {
    Assert.assertTrue(versionRule.isAccept(null));
  }

  @Test
  public void isMatch() {
    Assert.assertTrue(versionRule.isMatch(VersionConst.v1, VersionConst.v1));
    Assert.assertFalse(versionRule.isMatch(VersionConst.v2, VersionConst.v1));
  }
}
