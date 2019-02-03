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

import org.apache.servicecomb.config.priority.impl.BooleanPriorityProperty;
import org.apache.servicecomb.config.priority.impl.DoublePriorityProperty;
import org.apache.servicecomb.config.priority.impl.FloatPriorityProperty;
import org.apache.servicecomb.config.priority.impl.IntPriorityProperty;
import org.apache.servicecomb.config.priority.impl.LongPriorityProperty;
import org.apache.servicecomb.config.priority.impl.StringPriorityProperty;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPriorityProperty {
  String high = "ms.schema.op";

  String middle = "ms.schema";

  String low = "ms";

  String[] keys = {high, middle, low};

  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testLong() {
    LongPriorityProperty config = new LongPriorityProperty(-1L, -2L, keys);
    Assert.assertEquals(-2L, (long) config.getValue());

    ArchaiusUtils.setProperty(low, 1L);
    Assert.assertEquals(1L, (long) config.getValue());

    ArchaiusUtils.setProperty(middle, 2L);
    Assert.assertEquals(2L, (long) config.getValue());

    ArchaiusUtils.setProperty(high, 3L);
    Assert.assertEquals(3L, (long) config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(3L, (long) config.getValue());
    ArchaiusUtils.setProperty(middle, 2L);

    ArchaiusUtils.updateProperty(high, null);
    Assert.assertEquals(2L, (long) config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(1L, (long) config.getValue());

    ArchaiusUtils.updateProperty(low, null);
    Assert.assertEquals(-2L, (long) config.getValue());
  }

  @Test
  public void testInt() {
    IntPriorityProperty config = new IntPriorityProperty(-1, -2, keys);
    Assert.assertEquals(-2L, (int) config.getValue());

    ArchaiusUtils.setProperty(low, 1);
    Assert.assertEquals(1, (int) config.getValue());

    ArchaiusUtils.setProperty(middle, 2);
    Assert.assertEquals(2, (int) config.getValue());

    ArchaiusUtils.setProperty(high, 3);
    Assert.assertEquals(3, (int) config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(3, (int) config.getValue());
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.updateProperty(high, null);
    Assert.assertEquals(2, (int) config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(1, (int) config.getValue());

    ArchaiusUtils.updateProperty(low, null);
    Assert.assertEquals(-2, (int) config.getValue());
  }

  @Test
  public void testString() {
    StringPriorityProperty config = new StringPriorityProperty(null, "def", keys);
    Assert.assertEquals("def", config.getValue());

    ArchaiusUtils.setProperty(low, 1);
    Assert.assertEquals("1", config.getValue());

    ArchaiusUtils.setProperty(middle, 2);
    Assert.assertEquals("2", config.getValue());

    ArchaiusUtils.setProperty(high, 3);
    Assert.assertEquals("3", config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals("3", config.getValue());
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.updateProperty(high, null);
    Assert.assertEquals("2", config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals("1", config.getValue());

    ArchaiusUtils.updateProperty(low, null);
    Assert.assertEquals("def", config.getValue());
  }

  @Test
  public void testBoolean() {
    BooleanPriorityProperty config = new BooleanPriorityProperty(null, false, keys);
    Assert.assertFalse(config.getValue());

    ArchaiusUtils.setProperty(low, true);
    Assert.assertTrue(config.getValue());

    ArchaiusUtils.setProperty(middle, false);
    Assert.assertFalse(config.getValue());

    ArchaiusUtils.setProperty(high, true);
    Assert.assertTrue(config.getValue());

    ArchaiusUtils.updateProperty(middle, false);
    Assert.assertTrue(config.getValue());
    ArchaiusUtils.setProperty(middle, false);

    ArchaiusUtils.updateProperty(high, null);
    Assert.assertFalse(config.getValue());

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertTrue(config.getValue());

    ArchaiusUtils.updateProperty(low, null);
    Assert.assertFalse(config.getValue());
  }

  @Test
  public void testDouble() {
    DoublePriorityProperty config = new DoublePriorityProperty(null, -2.0, keys);
    Assert.assertEquals(-2, config.getValue(), 0);

    ArchaiusUtils.setProperty(low, 1);
    Assert.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, 2);
    Assert.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.setProperty(high, 3);
    Assert.assertEquals(3, config.getValue(), 0);

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(3, config.getValue(), 0);
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.updateProperty(high, null);
    Assert.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.updateProperty(low, null);
    Assert.assertEquals(-2, config.getValue(), 0);
  }

  @Test
  public void testFloat() {
    FloatPriorityProperty config = new FloatPriorityProperty(null, -2.0f, keys);
    Assert.assertEquals(-2, config.getValue(), 0);

    ArchaiusUtils.setProperty(low, 1);
    Assert.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.setProperty(middle, 2);
    Assert.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.setProperty(high, 3);
    Assert.assertEquals(3, config.getValue(), 0);

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(3, config.getValue(), 0);
    ArchaiusUtils.setProperty(middle, 2);

    ArchaiusUtils.updateProperty(high, null);
    Assert.assertEquals(2, config.getValue(), 0);

    ArchaiusUtils.updateProperty(middle, null);
    Assert.assertEquals(1, config.getValue(), 0);

    ArchaiusUtils.updateProperty(low, null);
    Assert.assertEquals(-2, config.getValue(), 0);
  }
}
