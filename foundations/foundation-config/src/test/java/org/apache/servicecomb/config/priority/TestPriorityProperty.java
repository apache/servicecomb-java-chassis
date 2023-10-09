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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestPriorityProperty extends TestPriorityPropertyBase {
  String high = "ms.schema.op";

  String middle = "ms.schema";

  String low = "ms";

  String[] keys = {high, middle, low};

  @Test
  public void testLong() {
    PriorityProperty<Long> config = propertyFactory.getOrCreate(Long.class, -1L, -2L, keys);
    Assertions.assertEquals(-2L, (long) config.getValue());

    updateLong(low, 1L, config);
    Assertions.assertEquals(1L, (long) config.getValue());

    updateLong(middle, 2L, config);
    Assertions.assertEquals(2L, (long) config.getValue());

    updateLong(high, 3L, config);
    Assertions.assertEquals(3L, (long) config.getValue());

    updateLong(middle, null, config);
    Assertions.assertEquals(3L, (long) config.getValue());

    updateLong(middle, 2L, config);
    updateLong(high, null, config);
    Assertions.assertEquals(2L, (long) config.getValue());

    updateLong(middle, null, config);
    Assertions.assertEquals(1L, (long) config.getValue());

    updateLong(low, null, config);
    Assertions.assertEquals(-2L, (long) config.getValue());
  }

  @Test
  public void testInt() {
    PriorityProperty<Integer> config = propertyFactory.getOrCreate(Integer.class, -1, -2, keys);
    Assertions.assertEquals(-2L, (int) config.getValue());

    updateInt(low, 1, config);
    Assertions.assertEquals(1, (int) config.getValue());

    updateInt(middle, 2, config);
    Assertions.assertEquals(2, (int) config.getValue());

    updateInt(high, 3, config);
    Assertions.assertEquals(3, (int) config.getValue());

    updateInt(middle, null, config);
    Assertions.assertEquals(3, (int) config.getValue());
    updateInt(middle, 2, config);

    updateInt(high, null, config);
    Assertions.assertEquals(2, (int) config.getValue());

    updateInt(middle, null, config);
    Assertions.assertEquals(1, (int) config.getValue());

    updateInt(low, null, config);
    Assertions.assertEquals(-2, (int) config.getValue());
  }

  private void updateFloat(String key, Float value, PriorityProperty<Float> config) {
    Mockito.when(environment.getProperty(key, Float.class)).thenReturn(value);
    config.updateValue();
  }

  private void updateStr(String key, String value, PriorityProperty<String> config) {
    Mockito.when(environment.getProperty(key)).thenReturn(value);
    config.updateValue();
  }

  private void updateInt(String key, Integer value, PriorityProperty<Integer> config) {
    Mockito.when(environment.getProperty(key, Integer.class)).thenReturn(value);
    config.updateValue();
  }

  private void updateLong(String key, Long value, PriorityProperty<Long> config) {
    Mockito.when(environment.getProperty(key, Long.class)).thenReturn(value);
    config.updateValue();
  }

  private void updateBoolean(String key, Boolean value, PriorityProperty<Boolean> config) {
    Mockito.when(environment.getProperty(key, Boolean.class)).thenReturn(value);
    config.updateValue();
  }

  private void updateDouble(String key, Double value, PriorityProperty<Double> config) {
    Mockito.when(environment.getProperty(key, Double.class)).thenReturn(value);
    config.updateValue();
  }

  @Test
  public void testString() {
    PriorityProperty<String> config = propertyFactory.getOrCreate(String.class, null, "def", keys);
    Assertions.assertEquals("def", config.getValue());

    updateStr(low, "1", config);
    Assertions.assertEquals("1", config.getValue());

    updateStr(middle, "2", config);
    Assertions.assertEquals("2", config.getValue());

    updateStr(high, "3", config);
    Assertions.assertEquals("3", config.getValue());

    updateStr(middle, null, config);
    Assertions.assertEquals("3", config.getValue());

    updateStr(middle, "2", config);
    updateStr(high, null, config);
    Assertions.assertEquals("2", config.getValue());

    updateStr(middle, null, config);
    Assertions.assertEquals("1", config.getValue());

    updateStr(low, null, config);
    Assertions.assertEquals("def", config.getValue());
  }

  @Test
  public void testBoolean() {
    PriorityProperty<Boolean> config = propertyFactory.getOrCreate(Boolean.class, null, false, keys);
    Assertions.assertFalse(config.getValue());

    updateBoolean(low, true, config);
    Assertions.assertTrue(config.getValue());

    updateBoolean(middle, false, config);
    Assertions.assertFalse(config.getValue());

    updateBoolean(high, true, config);
    Assertions.assertTrue(config.getValue());

    updateBoolean(middle, false, config);
    Assertions.assertTrue(config.getValue());

    updateBoolean(middle, false, config);
    updateBoolean(high, null, config);
    Assertions.assertFalse(config.getValue());

    updateBoolean(middle, null, config);
    Assertions.assertTrue(config.getValue());

    updateBoolean(low, null, config);
    Assertions.assertFalse(config.getValue());
  }

  @Test
  public void testDouble() {
    PriorityProperty<Double> config = propertyFactory.getOrCreate(Double.class, null, -2.0, keys);
    Assertions.assertEquals(-2, config.getValue(), 0);

    updateDouble(low, 1D, config);
    Assertions.assertEquals(1, config.getValue(), 0);

    updateDouble(middle, 2D, config);
    Assertions.assertEquals(2, config.getValue(), 0);

    updateDouble(high, 3D, config);
    Assertions.assertEquals(3, config.getValue(), 0);

    updateDouble(middle, null, config);
    Assertions.assertEquals(3, config.getValue(), 0);

    updateDouble(middle, 2D, config);
    updateDouble(high, null, config);
    Assertions.assertEquals(2, config.getValue(), 0);

    updateDouble(middle, null, config);
    Assertions.assertEquals(1, config.getValue(), 0);

    updateDouble(low, null, config);
    Assertions.assertEquals(-2, config.getValue(), 0);
  }

  @Test
  public void testFloat() {
    PriorityProperty<Float> config = propertyFactory.getOrCreate(Float.class, null, -2.0f, keys);
    Assertions.assertEquals(-2, config.getValue(), 0);

    updateFloat(low, 1F, config);
    Assertions.assertEquals(1, config.getValue(), 0);

    updateFloat(middle, 2F, config);
    Assertions.assertEquals(2, config.getValue(), 0);

    updateFloat(high, 3F, config);
    Assertions.assertEquals(3, config.getValue(), 0);

    updateFloat(middle, null, config);
    Assertions.assertEquals(3, config.getValue(), 0);

    updateFloat(middle, 2F, config);
    updateFloat(high, null, config);
    Assertions.assertEquals(2, config.getValue(), 0);

    updateFloat(middle, null, config);
    Assertions.assertEquals(1, config.getValue(), 0);

    updateFloat(low, null, config);
    Assertions.assertEquals(-2, config.getValue(), 0);
  }
}
