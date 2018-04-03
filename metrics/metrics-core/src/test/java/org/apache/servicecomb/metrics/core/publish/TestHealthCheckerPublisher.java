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

package org.apache.servicecomb.metrics.core.publish;

import java.util.Map;

import org.apache.servicecomb.foundation.metrics.health.HealthCheckResult;
import org.apache.servicecomb.foundation.metrics.health.HealthChecker;
import org.apache.servicecomb.foundation.metrics.health.HealthCheckerManager;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerRestPublisher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestHealthCheckerPublisher {
  private HealthChecker good = new HealthChecker() {
    @Override
    public String getName() {
      return "test";
    }

    @Override
    public HealthCheckResult check() {
      return new HealthCheckResult(true, "info", "extra data");
    }
  };

  private HealthChecker bad = new HealthChecker() {
    @Override
    public String getName() {
      return "test2";
    }

    @Override
    public HealthCheckResult check() {
      return new HealthCheckResult(false, "info2", "extra data 2");
    }
  };


  @Before
  public void reset() {
    HealthCheckerManager.getInstance().unregister(good.getName());
    HealthCheckerManager.getInstance().unregister(bad.getName());
  }

  @Test
  public void checkHealthGood() {
    HealthCheckerManager.getInstance().register(good);
    HealthCheckerRestPublisher publisher = new HealthCheckerRestPublisher();
    Assert.assertEquals(true, publisher.checkHealth());
  }

  @Test
  public void checkHealthBad() {
    HealthCheckerManager.getInstance().register(good);
    HealthCheckerManager.getInstance().register(bad);
    HealthCheckerRestPublisher publisher = new HealthCheckerRestPublisher();
    Assert.assertEquals(false, publisher.checkHealth());
  }

  @Test
  public void checkHealthDetails() {
    HealthCheckerManager.getInstance().register(good);
    HealthCheckerManager.getInstance().register(bad);
    HealthCheckerRestPublisher publisher = new HealthCheckerRestPublisher();
    Map<String, HealthCheckResult> content = publisher.checkHealthDetails();
    Assert.assertEquals(true, content.get("test").isHealthy());
    Assert.assertEquals("info", content.get("test").getInformation());
    Assert.assertEquals("extra data", content.get("test").getExtraData());
  }
}
