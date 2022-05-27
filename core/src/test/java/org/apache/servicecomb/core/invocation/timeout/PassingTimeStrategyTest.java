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

package org.apache.servicecomb.core.invocation.timeout;

import static org.apache.servicecomb.core.invocation.timeout.PassingTimeStrategy.CHAIN_START_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.time.MockClock;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

class PassingTimeStrategyTest {
  PassingTimeStrategy strategy = new PassingTimeStrategy();

  @Test
  void should_init_when_start_as_first_chain_node() {
    Invocation invocation = new Invocation();
    invocation.getInvocationStageTrace().setStartTimeMillis(10);

    strategy.start(invocation);

    assertThat(invocation.getContext(CHAIN_START_TIME)).isEqualTo("10");
    assertThat(invocation.<Long>getLocalContext(CHAIN_START_TIME)).isEqualTo(10L);
  }

  @Test
  void should_init_when_start_as_first_node_of_a_process_but_not_first_of_a_chain() {
    Invocation invocation = new Invocation();
    invocation.setContext(ImmutableMap.of(CHAIN_START_TIME, "10"));

    strategy.start(invocation);

    assertThat(invocation.getContext(CHAIN_START_TIME)).isEqualTo("10");
    assertThat(invocation.<Long>getLocalContext(CHAIN_START_TIME)).isEqualTo(10L);
  }

  @Test
  void should_do_nothing_when_start_not_as_first_node_of_a_process() {
    Invocation invocation = new Invocation();
    invocation.setContext(ImmutableMap.of());
    invocation.setLocalContext(ImmutableMap.of(CHAIN_START_TIME, 10L));

    Throwable throwable = catchThrowable(() -> strategy.start(invocation));

    assertThat(throwable).isNull();
  }

  @Test
  void should_calc_elapsed_time_as_passing_time() {
    Invocation invocation = new Invocation();
    invocation.addLocalContext(CHAIN_START_TIME, 10L);
    strategy.setClock(new MockClock(100L));

    long elapsedNanoTime = strategy.calculateElapsedNanoTime(invocation);

    assertThat(elapsedNanoTime).isEqualTo(TimeUnit.MILLISECONDS.toNanos(90));
  }
}