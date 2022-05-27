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

import java.time.Clock;
import java.util.Objects;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.TimeUtils;

public class TryingIsolatedServerMarker {
  Clock clock;

  final long startTryingTimestamp;

  private final Invocation invocation;

  /**
   * To make sure even if some error interrupted the releasing operation of
   * {@link ServiceCombServerStats#globalAllowIsolatedServerTryingFlag},
   * the state can still be recovered.
   */
  private final int maxSingleTestWindow;

  public TryingIsolatedServerMarker(Invocation invocation) {
    Objects.requireNonNull(invocation, "invocation should be non-null");
    this.invocation = invocation;
    clock = TimeUtils.getSystemDefaultZoneClock();
    startTryingTimestamp = clock.millis();
    maxSingleTestWindow = Configuration.INSTANCE.getMaxSingleTestWindow();
  }

  public boolean isOutdated() {
    return this.invocation.isFinished()
        || clock.millis() - startTryingTimestamp >= maxSingleTestWindow;
  }

  public Invocation getInvocation() {
    return invocation;
  }
}
