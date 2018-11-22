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

package org.apache.servicecomb.foundation.common.event;

import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

public class TestEventManager {
  private int objCount = 0;

  private int iCount = 0;

  public void test(Object listener) {
    EventManager.register(listener);
    EventManager.post("a");
    EventManager.post(1);
    Assert.assertEquals(2, objCount);
    Assert.assertEquals(1, iCount);

    EventManager.unregister(listener);
    EventManager.post("a");
    EventManager.post(1);
    Assert.assertEquals(2, objCount);
    Assert.assertEquals(1, iCount);
  }

  @Test
  public void lambda() {
    LogCollector collector = new LogCollector();

    test(this);
    Assert.assertTrue(collector.getEvents().isEmpty());
    collector.teardown();
  }

  @Test
  public void reflect() {
    LogCollector collector = new LogCollector();
    Object listener = new Object() {
      @Subscribe
      public void onObject(Object obj) {
        objCount++;
      }

      @Subscribe
      public void onInt(Integer obj) {
        iCount++;
      }
    };
    test(listener);
    Assert.assertThat(collector.getEvents().stream().map(e -> e.getMessage()).collect(Collectors.toList()),
        Matchers.containsInAnyOrder(
            "Failed to create lambda for method: public void org.apache.servicecomb.foundation.common.event.TestEventManager$1.onObject(java.lang.Object), fallback to reflect.",
            "Failed to create lambda for method: public void org.apache.servicecomb.foundation.common.event.TestEventManager$1.onInt(java.lang.Integer), fallback to reflect."));

    collector.teardown();
  }

  @Subscribe
  public void onObject(Object obj) {
    objCount++;
  }

  @Subscribe
  public void onInt(Integer obj) {
    iCount++;
  }
}

