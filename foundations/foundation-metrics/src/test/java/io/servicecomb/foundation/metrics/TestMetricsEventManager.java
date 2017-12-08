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
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.metrics.event.MetricsEvent;
import io.servicecomb.foundation.metrics.event.MetricsEventListener;
import io.servicecomb.foundation.metrics.event.MetricsEventManager;

public class TestMetricsEventManager {

  @Test
  public void testManager() {
    AtomicBoolean eventReceived = new AtomicBoolean(false);

    MetricsEventManager.registerEventListener(new MetricsEventListener() {
      @Override
      public Class<? extends MetricsEvent> getConcernedEvent() {
        return TestMetricsEvent.class;
      }

      @Override
      public void process(MetricsEvent data) {
        eventReceived.set(true);
      }
    });

    MetricsEventManager.triggerEvent(new TestMetricsEvent());

    await().atMost(1, TimeUnit.SECONDS)
        .until(eventReceived::get);

    Assert.assertTrue(eventReceived.get());
  }

  private class TestMetricsEvent implements MetricsEvent {
  }
}
