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

package org.apache.servicecomb.foundation.metrics.health;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestHealthCheckerManager {

  private HealthChecker good = new HealthChecker() {
    @Override
    public String getName() {
      return "testBad";
    }

    @Override
    public HealthCheckResult check() {
      return new HealthCheckResult(false, "bad", "bad component");
    }
  };

  private HealthChecker bad = new HealthChecker() {
    @Override
    public String getName() {
      return "testGood";
    }

    @Override
    public HealthCheckResult check() {
      return new HealthCheckResult(true, "good", "good component");
    }
  };

  @Before
  public void reset() {
    HealthCheckerManager.getInstance().unregister(good.getName());
    HealthCheckerManager.getInstance().unregister(bad.getName());
  }

  @Test
  public void checkResultCount_None() {
    Map<String, HealthCheckResult> results = HealthCheckerManager.getInstance().check();
    Assert.assertEquals(0, results.size());
  }

  @Test
  public void checkResultCount_One() {
    HealthCheckerManager.getInstance().register(good);
    Map<String, HealthCheckResult> results = HealthCheckerManager.getInstance().check();
    Assert.assertEquals(1, results.size());
  }

  @Test
  public void checkResultCount_Both() {
    HealthCheckerManager.getInstance().register(good);
    HealthCheckerManager.getInstance().register(bad);
    Map<String, HealthCheckResult> results = HealthCheckerManager.getInstance().check();
    Assert.assertEquals(2, results.size());
  }

  @Test
  public void checkGoodResult() {
    HealthCheckerManager.getInstance().register(good);
    HealthCheckerManager.getInstance().register(bad);
    HealthCheckResult result = HealthCheckerManager.getInstance().check().get("testGood");
    Assert.assertEquals(true, result.isHealthy());
    Assert.assertEquals("good", result.getInformation());
    Assert.assertEquals("good component", result.getExtraData());
  }

  @Test
  public void checkBadResult() {
    HealthCheckerManager.getInstance().register(good);
    HealthCheckerManager.getInstance().register(bad);
    HealthCheckResult result = HealthCheckerManager.getInstance().check().get("testBad");
    Assert.assertEquals(false, result.isHealthy());
    Assert.assertEquals("bad", result.getInformation());
    Assert.assertEquals("bad component", result.getExtraData());
  }
}
