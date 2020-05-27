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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.Collections;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.MockMicroserviceVersions;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMicroserviceVersionRuleLatest {
  MockMicroserviceVersions mockMicroserviceVersions;

  MicroserviceVersionRule microserviceVersionRule;

  @Before
  public void setup() {
    ConfigUtil.installDynamicConfig();
    mockMicroserviceVersions = new MockMicroserviceVersions();
    microserviceVersionRule = mockMicroserviceVersions
        .getOrCreateMicroserviceVersionRule(DefinitionConst.VERSION_RULE_LATEST);
  }

  @AfterClass
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getVersionRule() {
    Assert.assertEquals(DefinitionConst.VERSION_RULE_LATEST, microserviceVersionRule.getVersionRule().getVersionRule());
  }

  @Test
  public void update_empty() {
    Assert.assertNull(microserviceVersionRule.getLatestMicroserviceVersion());
  }

  @Test
  public void update_v1() {
    mockMicroserviceVersions.update_v1();

    mockMicroserviceVersions.check(microserviceVersionRule, "1.0.0", "1.0.0");
  }

  @Test
  public void update_v1_then_v2() {
    mockMicroserviceVersions.update_v1_then_v2();

    mockMicroserviceVersions.check(microserviceVersionRule, "2.0.0", "2.0.0");
  }

  @Test
  public void update_v1_then_v2_then_v1() {
    mockMicroserviceVersions.update_v1_then_v2_then_v1();

    mockMicroserviceVersions.check(microserviceVersionRule, "1.0.0", "1.0.0");
  }

  @Test
  public void update_v1_then_all() {
    mockMicroserviceVersions.update_v1_then_all();

    mockMicroserviceVersions.check(microserviceVersionRule, "4.0.0", "4.0.0");
  }

  @Test
  public void update_all() {
    mockMicroserviceVersions.update_all();

    mockMicroserviceVersions.check(microserviceVersionRule, "4.0.0", "4.0.0");
  }

  @Test
  public void update_all_then_v3() {
    mockMicroserviceVersions.update_all_then_v3();

    mockMicroserviceVersions.check(microserviceVersionRule, "3.0.0", "3.0.0");
  }

  @Test
  public void update_all_then_empty() {
    mockMicroserviceVersions.update_all();
    mockMicroserviceVersions.update(Collections.emptyList());

    mockMicroserviceVersions.check(microserviceVersionRule, "4.0.0");
  }
}
