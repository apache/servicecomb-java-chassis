/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.metrics.event.MetricsEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventListener;
import io.servicecomb.foundation.metrics.event.MetricsEventManager;

public class TestMetricsEventManager {

  private static int value = 0;

  private static int ignore = 0;

  @Test
  public void testManager() {
    MetricsEventManager.registerEventListener(new MetricsEventListenerForTest());

    MetricsEventManager.triggerEvent(new MetricsEventForTest(100));

    await().atMost(1, TimeUnit.SECONDS).until(() -> value == 100);

    Assert.assertTrue(value == 100);
    Assert.assertTrue(ignore == 0);
  }

  class MetricsEventForTest implements MetricsEvent {
    private final int data;

    public int getData() {
      return data;
    }

    public MetricsEventForTest(int data) {
      this.data = data;
    }
  }

  class MetricsEventForIgnore implements MetricsEvent {
    private final int data;

    public int getData() {
      return data;
    }

    public MetricsEventForIgnore(int data) {
      this.data = data;
    }
  }

  class MetricsEventListenerForTest implements MetricsEventListener {
    @Override
    public Class<? extends MetricsEvent> getConcernedEvent() {
      return MetricsEventForTest.class;
    }

    @Override
    public void process(MetricsEvent data) {
      value = ((MetricsEventForTest) data).getData();
    }
  }
}
