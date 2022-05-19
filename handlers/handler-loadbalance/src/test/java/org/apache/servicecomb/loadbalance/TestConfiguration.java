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

package org.apache.servicecomb.loadbalance;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import org.junit.jupiter.api.Assertions;

/**
 *
 */
public class TestConfiguration {
  @After
  public void after() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testConstants() {

    Assertions.assertEquals("servicecomb.loadbalance.", Configuration.ROOT);
    Assertions.assertEquals("ribbon.", Configuration.ROOT_20);
    Assertions.assertEquals("SessionStickinessRule.successiveFailedTimes", Configuration.SUCCESSIVE_FAILED_TIMES);
    Assertions.assertEquals("maxSingleTestWindow", Configuration.FILTER_MAX_SINGLE_TEST_WINDOW);

    Assertions.assertNotNull(Configuration.INSTANCE);
  }

  @Test
  public void testFullConfigurationWithArgsString() {
    Assertions.assertNotNull(Configuration.INSTANCE.getSuccessiveFailedTimes("test"));
    Assertions.assertNotNull(Configuration.INSTANCE.getSessionTimeoutInSeconds("test"));
  }

  @Test
  public void testGetSuccessiveFailedTimes() {
    Assertions.assertNotNull(Configuration.INSTANCE.getSuccessiveFailedTimes("test"));
  }

  @Test
  public void testGetSessionTimeoutInSeconds() {
    Assertions.assertNotNull(Configuration.INSTANCE.getSessionTimeoutInSeconds("test"));
  }


  /**
   * The property key of  timerIntervalInMilis  changed from <code>servicecomb.loadbalance.stats.timerIntervalInMilis</code>
   * to <code>servicecomb.loadbalance.stats.timerIntervalInMillis</code>, check the compatibility
   */
  @Test
  public void testGetTimerIntervalInMillis() {
    System.setProperty(Configuration.TIMER_INTERVAL_IN_MILLIS, "100");
    ConcurrentCompositeConfiguration localConfiguration = ConfigUtil.createLocalConfig();
    Assertions.assertEquals("100", localConfiguration.getProperty(Configuration.TIMER_INTERVAL_IN_MILLIS));

    System.clearProperty(Configuration.TIMER_INTERVAL_IN_MILLIS);
    localConfiguration = ConfigUtil.createLocalConfig();
    Assertions.assertNull(localConfiguration.getProperty(Configuration.TIMER_INTERVAL_IN_MILLIS));

    System.setProperty("servicecomb.loadbalance.stats.timerIntervalInMilis", "100");
    localConfiguration = ConfigUtil.createLocalConfig();
    Assertions.assertEquals("100", localConfiguration.getProperty(Configuration.TIMER_INTERVAL_IN_MILLIS));
  }

  @Test
  public void testGetMaxSingleTestWindow() {
    Assertions.assertEquals(60000, Configuration.INSTANCE.getMaxSingleTestWindow());

    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.maxSingleTestWindow", 5000);
    Assertions.assertEquals(5000, Configuration.INSTANCE.getMaxSingleTestWindow());
  }
}
