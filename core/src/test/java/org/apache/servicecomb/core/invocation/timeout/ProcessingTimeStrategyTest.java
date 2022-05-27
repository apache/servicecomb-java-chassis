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

import static org.apache.servicecomb.core.invocation.timeout.ProcessingTimeStrategy.CHAIN_PROCESSING;
import static org.apache.servicecomb.core.invocation.timeout.ProcessingTimeStrategy.CHAIN_START_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.time.MockTicker;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

class ProcessingTimeStrategyTest {
  ProcessingTimeStrategy strategy = new ProcessingTimeStrategy();

  @Test
  void should_init_when_start_as_first_chain_node() {
    Invocation invocation = new Invocation();
    invocation.getInvocationStageTrace().start(10);

    strategy.start(invocation);

    assertThat(invocation.<Long>getLocalContext(CHAIN_START_TIME)).isEqualTo(10L);
    assertThat(invocation.<Long>getLocalContext(CHAIN_PROCESSING)).isEqualTo(0L);
  }

  @Test
  void should_do_nothing_when_not_first_node_of_a_process() {
    Invocation invocation = new Invocation();
    invocation.setLocalContext(ImmutableMap.of(
        CHAIN_START_TIME, 10L,
        CHAIN_PROCESSING, 0L
    ));

    Throwable throwable = catchThrowable(() -> strategy.start(invocation));

    assertThat(throwable).isNull();
  }

  @Test
  void should_calc_elapsed_time_as_processing_time() {
    strategy.setTicker(new MockTicker(50L));

    Invocation invocation = new Invocation();
    invocation.addLocalContext(CHAIN_START_TIME, 10L);
    invocation.addLocalContext(CHAIN_PROCESSING, 20L);

    long elapsedNanoTime = strategy.calculateElapsedNanoTime(invocation);

    assertThat(elapsedNanoTime).isEqualTo(60L);
  }

  @Test
  void should_update_processing_time_before_send() {
    strategy = new ProcessingTimeStrategy() {
      @Override
      public void checkTimeout(Invocation invocation) {

      }
    };
    strategy.setTicker(new MockTicker(50L));

    Invocation invocation = new Invocation();
    invocation.addLocalContext(CHAIN_START_TIME, 10L);
    invocation.addLocalContext(CHAIN_PROCESSING, 20L);

    strategy.beforeSendRequest(invocation);

    assertThat(invocation.getContext(CHAIN_PROCESSING)).isEqualTo("60");
  }
}