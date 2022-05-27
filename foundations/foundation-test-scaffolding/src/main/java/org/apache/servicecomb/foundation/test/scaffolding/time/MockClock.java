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

package org.apache.servicecomb.foundation.test.scaffolding.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class MockClock extends Clock {
  private MockValues<Long> values;

  public MockClock() {
    this(0L);
  }

  public MockClock(Long... values) {
    this.setValues(values);
  }

  public MockClock setValues(Long... values) {
    this.values = new MockValues<Long>()
        .setDefaultValue(0L)
        .setValues(values);
    return this;
  }

  @Override
  public ZoneId getZone() {
    return null;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return null;
  }

  @Override
  public Instant instant() {
    return null;
  }

  @Override
  public long millis() {
    return values.read();
  }
}
