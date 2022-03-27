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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;

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

    assertEquals("servicecomb.loadbalance.", Configuration.ROOT);
    assertEquals("ribbon.", Configuration.ROOT_20);
    assertEquals("SessionStickinessRule.successiveFailedTimes", Configuration.SUCCESSIVE_FAILED_TIMES);
    assertEquals("maxSingleTestWindow", Configuration.FILTER_MAX_SINGLE_TEST_WINDOW);

    assertNotNull(Configuration.INSTANCE);
  }

  @Test
  public void testFullConfigurationWithArgsString() {
    assertNotNull(Configuration.INSTANCE.getSuccessiveFailedTimes("test"));
    assertNotNull(Configuration.INSTANCE.getSessionTimeoutInSeconds("test"));
  }

  @Test
  public void testGetSuccessiveFailedTimes() {
    assertNotNull(Configuration.INSTANCE.getSuccessiveFailedTimes("test"));
  }

  @Test
  public void testGetSessionTimeoutInSeconds() {
    assertNotNull(Configuration.INSTANCE.getSessionTimeoutInSeconds("test"));
  }


  /**
   * The property key of  timerIntervalInMilis  changed from <code>servicecomb.loadbalance.stats.timerIntervalInMilis</code>
   * to <code>servicecomb.loadbalance.stats.timerIntervalInMillis</code>, check the compatibility
   */
  @Test
  public void testGetTimerIntervalInMillis() {
    System.setProperty(Configuration.TIMER_INTERVAL_IN_MILLIS, "100");
    ConcurrentCompositeConfiguration localConfiguration = ConfigUtil.createLocalConfig();
    assertEquals("100", localConfiguration.getProperty(Configuration.TIMER_INTERVAL_IN_MILLIS));

    System.clearProperty(Configuration.TIMER_INTERVAL_IN_MILLIS);
    localConfiguration = ConfigUtil.createLocalConfig();
    assertNull(localConfiguration.getProperty(Configuration.TIMER_INTERVAL_IN_MILLIS));

    System.setProperty("servicecomb.loadbalance.stats.timerIntervalInMilis", "100");
    localConfiguration = ConfigUtil.createLocalConfig();
    assertEquals("100", localConfiguration.getProperty(Configuration.TIMER_INTERVAL_IN_MILLIS));
  }

  @Test
  public void testGetMaxSingleTestWindow() {
    assertEquals(60000, Configuration.INSTANCE.getMaxSingleTestWindow());

    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.maxSingleTestWindow", 5000);
    assertEquals(5000, Configuration.INSTANCE.getMaxSingleTestWindow());
  }
}
