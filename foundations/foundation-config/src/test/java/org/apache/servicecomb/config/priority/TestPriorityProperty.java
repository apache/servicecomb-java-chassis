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

import java.util.Collections;

import org.apache.commons.configuration.MapConfiguration;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

public class TestPriorityProperty extends TestPriorityPropertyBase {
  String high = "ms.schema.op";

  String middle = "ms.schema";

  String low = "ms";

  String[] keys = {high, middle, low};

  @Test
  public void testLong() {
    PriorityProperty<Long> config = propertyFactory.getOrCreate(Long.class, -1L, -2L, keys);
    Assertions.assertEquals(-2L, (long) config.getValue());

    ArchaiusUtils.setProperty(low, 1L);
    Assertions.assertEquals(1L, (long) config.getValue());

    ArchaiusUtils.setProperty(middle, 2L);
    Assertions.assertEquals(2L, (long) config.getValue());

    ArchaiusUtils.setProperty(high, 3L);
    Assertions.assertEquals(3L, (long) config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(3L, (long) config.getValue());
    ArchaiusUtils.setProperty(middle, 2L);

    ArchaiusUtils.setProperty(high, null);
    Assertions.assertEquals(2L, (long) config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(1L, (long) config.getValue());

    ArchaiusUtils.setProperty(low, null);
    Assertions.assertEquals(-2L, (long) config.getValue());
  }

  @Test
  public void testInt() {
    PriorityProperty<Integer> config = propertyFactory.getOrCreate(Integer.class, -1, -2, keys);
    Assertions.assertEquals(-2L, (int) config.getValue());

    ArchaiusUtils.setProperty(low, 1);
    Assertions.assertEquals(1, (int) config.getValue());

    ArchaiusUtils.setProperty(middle, 2);
    Assertions.assertEquals(2, (int) config.getValue());

    ArchaiusUtils.setProperty(high, 3);
    Assertions.assertEquals(3, (int) config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(3, (int) config.getValue());
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.setProperty(high, null);
    Assertions.assertEquals(2, (int) config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(1, (int) config.getValue());

    ArchaiusUtils.setProperty(low, null);
    Assertions.assertEquals(-2, (int) config.getValue());
  }

  @Test
  public void testString() {
    PriorityProperty<String> config = propertyFactory.getOrCreate(String.class, null, "def", keys);
    Assertions.assertEquals("def", config.getValue());

    ArchaiusUtils.setProperty(low, 1);
    Assertions.assertEquals("1", config.getValue());

    ArchaiusUtils.setProperty(middle, 2);
    Assertions.assertEquals("2", config.getValue());

    ArchaiusUtils.setProperty(high, 3);
    Assertions.assertEquals("3", config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals("3", config.getValue());
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.setProperty(high, null);
    Assertions.assertEquals("2", config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals("1", config.getValue());

    ArchaiusUtils.setProperty(low, null);
    Assertions.assertEquals("def", config.getValue());
  }

  @Test
  public void testBoolean() {
    PriorityProperty<Boolean> config = propertyFactory.getOrCreate(Boolean.class, null, false, keys);
    Assertions.assertFalse(config.getValue());

    ArchaiusUtils.setProperty(low, true);
    Assertions.assertTrue(config.getValue());

    ArchaiusUtils.setProperty(middle, false);
    Assertions.assertFalse(config.getValue());

    ArchaiusUtils.setProperty(high, true);
    Assertions.assertTrue(config.getValue());

    ArchaiusUtils.setProperty(middle, false);
    Assertions.assertTrue(config.getValue());
    ArchaiusUtils.setProperty(middle, false);

    ArchaiusUtils.setProperty(high, null);
    Assertions.assertFalse(config.getValue());

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertTrue(config.getValue());

    ArchaiusUtils.setProperty(low, null);
    Assertions.assertFalse(config.getValue());
  }

  @Test
  public void testDouble() {
    PriorityProperty<Double> config = propertyFactory.getOrCreate(Double.class, null, -2.0, keys);
    Assertions.assertEquals(-2, config.getValue(), 0);

    ArchaiusUtils.setProperty(low, 1);
    Assertions.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, 2);
    Assertions.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.setProperty(high, 3);
    Assertions.assertEquals(3, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(3, config.getValue(), 0);
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.setProperty(high, null);
    Assertions.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.setProperty(low, null);
    Assertions.assertEquals(-2, config.getValue(), 0);
  }

  @Test
  public void testFloat() {
    PriorityProperty<Float> config = propertyFactory.getOrCreate(Float.class, null, -2.0f, keys);
    Assertions.assertEquals(-2, config.getValue(), 0);

    ArchaiusUtils.setProperty(low, 1);
    Assertions.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, 2);
    Assertions.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.setProperty(high, 3);
    Assertions.assertEquals(3, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(3, config.getValue(), 0);
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.setProperty(high, null);
    Assertions.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, null);
    Assertions.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.setProperty(low, null);
    Assertions.assertEquals(-2, config.getValue(), 0);
  }

  @Test
  public void globalRefresh() {
    PriorityProperty<String> property = propertyFactory.getOrCreate(String.class, null, null, keys);

    Assertions.assertNull(property.getValue());

    ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) DynamicPropertyFactory
        .getBackingConfigurationSource();
    config.addConfiguration(new MapConfiguration(Collections.singletonMap(high, "high-value")));

    Assertions.assertEquals("high-value", property.getValue());
  }
}
