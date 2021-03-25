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

package org.apache.servicecomb.config.priority;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestPriorityPropertyFactory extends TestPriorityPropertyBase {
  @Test
  void should_not_create_multiple_instances_for_same_parameter() {
    PriorityProperty<Integer> p1 = propertyFactory.getOrCreate(int.class, null, 0, "high", "low");
    PriorityProperty<Integer> p2 = propertyFactory.getOrCreate(int.class, null, 0, "high", "low");

    assertThat(p1).isSameAs(p2);
    assertThat(propertyFactory.getProperties().count()).isEqualTo(1);
  }

  @Test
  void should_create_different_instances_for_different_parameter() {
    PriorityProperty<Integer> p1 = propertyFactory.getOrCreate(int.class, null, 0, "high", "low");
    PriorityProperty<Long> p2 = propertyFactory.getOrCreate(long.class, null, 0L, "high", "low");

    assertThat(p1).isNotSameAs(p2);
    assertThat(propertyFactory.getProperties().count()).isEqualTo(2);
  }
}