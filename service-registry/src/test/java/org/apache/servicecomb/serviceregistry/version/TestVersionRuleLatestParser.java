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
import org.junit.Assert;
import org.junit.Test;

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
