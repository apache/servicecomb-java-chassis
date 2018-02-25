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

package org.apache.servicecomb.metrics.core;

import java.util.Map;

import org.apache.servicecomb.foundation.metrics.health.HealthCheckResult;
import org.apache.servicecomb.foundation.metrics.health.HealthChecker;
import org.apache.servicecomb.foundation.metrics.health.HealthCheckerManager;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerPublisher;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHealthCheckerPublisher {

  @BeforeClass
  public static void setup() {
    HealthCheckerManager.getInstance().register(new HealthChecker() {
      @Override
      public String getName() {
        return "test";
      }

      @Override
      public HealthCheckResult check() {
        return new HealthCheckResult(true, "info", "extra data");
      }
    });

    HealthCheckerManager.getInstance().register(new HealthChecker() {
      @Override
      public String getName() {
        return "test2";
      }

      @Override
      public HealthCheckResult check() {
        return new HealthCheckResult(false, "info2", "extra data 2");
      }
    });
  }

  @Test
  public void checkHealth() {
    HealthCheckerPublisher publisher = new HealthCheckerPublisher();
    Assert.assertEquals(false, publisher.checkHealth());
  }

  @Test
  public void checkHealthDetails() {
    HealthCheckerPublisher publisher = new HealthCheckerPublisher();
    Map<String, HealthCheckResult> content = publisher.checkHealthDetails();
    Assert.assertEquals(true, content.get("test").isHealthy());
    Assert.assertEquals("info", content.get("test").getInformation());
    Assert.assertEquals("extra data", content.get("test").getExtraData());
  }
}
