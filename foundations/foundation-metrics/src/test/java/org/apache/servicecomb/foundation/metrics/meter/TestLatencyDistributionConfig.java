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
package org.apache.servicecomb.foundation.metrics.meter;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestLatencyDistributionConfig {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testValidProperty() {
    String validProperty1 = "0,1,2,10";
    String validProperty2 = "0,1,  2  , 10 ";
    String validProperty3 = "0,1,2,10,";

    LatencyDistributionConfig config1 = new LatencyDistributionConfig(validProperty1);
    LatencyDistributionConfig config2 = new LatencyDistributionConfig(validProperty2);
    LatencyDistributionConfig config3 = new LatencyDistributionConfig(validProperty3);

    Assert.assertEquals(4, config1.getScopeConfigs().size());
    Assert.assertEquals(4, config2.getScopeConfigs().size());
    Assert.assertEquals(4, config3.getScopeConfigs().size());
  }

  @Test
  public void testInValidProperty1() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("invalid latency scope, min=2, max=1.");
    LatencyDistributionConfig latencyDistributionConfig = new LatencyDistributionConfig("2,1,10");
    Assert.assertEquals(0, latencyDistributionConfig.getScopeConfigs().size());
  }

  @Test
  public void testInValidProperty2() {
    expectedException.expect(NumberFormatException.class);
    expectedException.expectMessage("For input string: \"a\"");
    LatencyDistributionConfig latencyDistributionConfig = new LatencyDistributionConfig("a,1,10");
    Assert.assertEquals(0, latencyDistributionConfig.getScopeConfigs().size());
  }
}
