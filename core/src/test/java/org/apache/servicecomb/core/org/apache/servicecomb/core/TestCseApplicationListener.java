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
package org.apache.servicecomb.core.org.apache.servicecomb.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.CseApplicationListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.event.ContextClosedEvent;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestCseApplicationListener {
  @Test
  public void onApplicationEvent_close(@Mocked ContextClosedEvent contextClosedEvent) {
    AtomicInteger count = new AtomicInteger();
    SCBEngine scbEngine = new SCBEngine() {
      @Override
      public synchronized void destroy() {
        count.incrementAndGet();
      }
    };
    new MockUp<SCBEngine>() {
      @Mock
      SCBEngine getInstance() {
        return scbEngine;
      }
    };
    scbEngine.setStatus(SCBStatus.UP);

    CseApplicationListener listener = new CseApplicationListener();

    listener.onApplicationEvent(contextClosedEvent);

    Assert.assertEquals(1, count.get());
  }
}
