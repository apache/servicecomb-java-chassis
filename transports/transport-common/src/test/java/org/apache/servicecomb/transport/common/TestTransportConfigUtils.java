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
package org.apache.servicecomb.transport.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class TestTransportConfigUtils {
  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  static String key = "verticle-count";

  static String deprecatedKey = "thread-count";

  @Test
  public void readVerticleCount_new_exist() {
    ArchaiusUtils.setProperty(key, 10);

    Assert.assertEquals(10, TransportConfigUtils.readVerticleCount(key, deprecatedKey));
  }

  @Test
  public void readVerticleCount_old_exist() {
    ArchaiusUtils.setProperty(deprecatedKey, 10);

    LogCollector collector = new LogCollector();
    Assert.assertEquals(10, TransportConfigUtils.readVerticleCount(key, deprecatedKey));
    Assert.assertEquals("thread-count is ambiguous, and deprecated, recommended to use verticle-count.",
        collector.getEvents().get(0).getMessage());
    collector.teardown();
  }

  @Test
  public void readVerticleCount_default_smallCpu() {
    new MockUp<Runtime>() {
      @Mock
      int availableProcessors() {
        return 7;
      }
    };

    LogCollector collector = new LogCollector();
    Assert.assertEquals(7, TransportConfigUtils.readVerticleCount(key, deprecatedKey));
    Assert.assertEquals("verticle-count not defined, set to 7.",
        collector.getLastEvents().getMessage());
    collector.teardown();
  }

  @Test
  public void readVerticleCount_default_bigCpu() {
    AtomicInteger count = new AtomicInteger(8);
    new MockUp<Runtime>() {
      @Mock
      int availableProcessors() {
        return count.get();
      }
    };

    LogCollector collector = new LogCollector();
    Assert.assertEquals(8, TransportConfigUtils.readVerticleCount(key, deprecatedKey));
    Assert.assertEquals("verticle-count not defined, set to 8.",
        collector.getLastEvents().getMessage());

    count.set(9);
    collector.clear();
    Assert.assertEquals(8, TransportConfigUtils.readVerticleCount(key, deprecatedKey));
    Assert.assertEquals("verticle-count not defined, set to 8.",
        collector.getLastEvents().getMessage());

    collector.teardown();
  }
}
