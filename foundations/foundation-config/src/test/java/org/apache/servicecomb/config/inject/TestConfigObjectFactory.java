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
package org.apache.servicecomb.config.inject;

import java.util.Arrays;

import org.apache.servicecomb.config.priority.TestPriorityPropertyBase;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestConfigObjectFactory extends TestPriorityPropertyBase {
  public static class ConfigNoAnnotation {
    public String strValue;

    private String strValue1;

    public int intValue;

    private int intValue1;

    public Integer intValueObj;

    private Integer intValueObj1;

    public long longValue;

    private long longValue1;

    public Long longValueObj;

    private Long longValueObj1;

    public float floatValue;

    private float floatValue1;

    public Float floatValueObj;

    private Float floatValueObj1;

    public double doubleValue;

    private double doubleValue1;

    public Double doubleValueObj;

    private Double doubleValueObj1;

    public boolean booleanValue;

    private boolean booleanValue1;

    public Boolean booleanValueObj;

    private Boolean booleanValueObj1;

    public String getStrValue1() {
      return strValue1;
    }

    public void setStrValue1(String strValue1) {
      this.strValue1 = strValue1;
    }

    public int getIntValue1() {
      return intValue1;
    }

    public void setIntValue1(int intValue1) {
      this.intValue1 = intValue1;
    }

    public Integer getIntValueObj1() {
      return intValueObj1;
    }

    public void setIntValueObj1(Integer intValueObj1) {
      this.intValueObj1 = intValueObj1;
    }

    public long getLongValue1() {
      return longValue1;
    }

    public void setLongValue1(long longValue1) {
      this.longValue1 = longValue1;
    }

    public Long getLongValueObj1() {
      return longValueObj1;
    }

    public void setLongValueObj1(Long longValueObj1) {
      this.longValueObj1 = longValueObj1;
    }

    public float getFloatValue1() {
      return floatValue1;
    }

    public void setFloatValue1(float floatValue1) {
      this.floatValue1 = floatValue1;
    }

    public Float getFloatValueObj1() {
      return floatValueObj1;
    }

    public void setFloatValueObj1(Float floatValueObj1) {
      this.floatValueObj1 = floatValueObj1;
    }

    public double getDoubleValue1() {
      return doubleValue1;
    }

    public void setDoubleValue1(double doubleValue1) {
      this.doubleValue1 = doubleValue1;
    }

    public Double getDoubleValueObj1() {
      return doubleValueObj1;
    }

    public void setDoubleValueObj1(Double doubleValueObj1) {
      this.doubleValueObj1 = doubleValueObj1;
    }

    public boolean isBooleanValue1() {
      return booleanValue1;
    }

    public void setBooleanValue1(boolean booleanValue1) {
      this.booleanValue1 = booleanValue1;
    }

    public Boolean getBooleanValueObj1() {
      return booleanValueObj1;
    }

    public void setBooleanValueObj1(Boolean booleanValueObj1) {
      this.booleanValueObj1 = booleanValueObj1;
    }
  }

  @Test
  public void noAnnotation_defaultValue() {
    ConfigNoAnnotation config = priorityPropertyManager.createConfigObject(ConfigNoAnnotation.class);

    Assertions.assertNull(config.strValue);
    Assertions.assertNull(config.getStrValue1());

    Assertions.assertEquals(0, config.intValue);
    Assertions.assertEquals(0, config.getIntValue1());
    Assertions.assertNull(config.intValueObj);
    Assertions.assertNull(config.getIntValueObj1());

    Assertions.assertEquals(0, config.longValue);
    Assertions.assertEquals(0, config.getLongValue1());
    Assertions.assertNull(config.longValueObj);
    Assertions.assertNull(config.getLongValueObj1());

    Assertions.assertEquals(0, config.floatValue, 0);
    Assertions.assertEquals(0, config.getFloatValue1(), 0);
    Assertions.assertNull(config.floatValueObj);
    Assertions.assertNull(config.getFloatValueObj1());

    Assertions.assertEquals(0, config.doubleValue, 0);
    Assertions.assertEquals(0, config.getDoubleValue1(), 0);
    Assertions.assertNull(config.doubleValueObj);
    Assertions.assertNull(config.getDoubleValueObj1());

    Assertions.assertFalse(config.booleanValue);
    Assertions.assertFalse(config.isBooleanValue1());
    Assertions.assertNull(config.booleanValueObj);
    Assertions.assertNull(config.getBooleanValueObj1());
  }

  @Test
  public void noAnnotation_initValue() {
    ArchaiusUtils.setProperty("strValue", "strValue");
    ArchaiusUtils.setProperty("strValue1", "strValue1");

    ArchaiusUtils.setProperty("intValue", 1);
    ArchaiusUtils.setProperty("intValue1", 2);
    ArchaiusUtils.setProperty("intValueObj", 3);
    ArchaiusUtils.setProperty("intValueObj1", 4);

    ArchaiusUtils.setProperty("longValue", 5);
    ArchaiusUtils.setProperty("longValue1", 6);
    ArchaiusUtils.setProperty("longValueObj", 7);
    ArchaiusUtils.setProperty("longValueObj1", 8);

    ArchaiusUtils.setProperty("floatValue", 9.0);
    ArchaiusUtils.setProperty("floatValue1", 10.0);
    ArchaiusUtils.setProperty("floatValueObj", 11.0);
    ArchaiusUtils.setProperty("floatValueObj1", 12.0);

    ArchaiusUtils.setProperty("doubleValue", 13.0);
    ArchaiusUtils.setProperty("doubleValue1", 14.0);
    ArchaiusUtils.setProperty("doubleValueObj", 15.0);
    ArchaiusUtils.setProperty("doubleValueObj1", 16.0);

    ArchaiusUtils.setProperty("booleanValue", true);
    ArchaiusUtils.setProperty("booleanValue1", true);
    ArchaiusUtils.setProperty("booleanValueObj", true);
    ArchaiusUtils.setProperty("booleanValueObj1", true);

    ConfigNoAnnotation config = priorityPropertyManager.createConfigObject(ConfigNoAnnotation.class);

    Assertions.assertEquals("strValue", config.strValue);
    Assertions.assertEquals("strValue1", config.getStrValue1());

    Assertions.assertEquals(1, config.intValue);
    Assertions.assertEquals(2, config.getIntValue1());
    Assertions.assertEquals(3, (int) config.intValueObj);
    Assertions.assertEquals(4, (int) config.getIntValueObj1());

    Assertions.assertEquals(5, config.longValue);
    Assertions.assertEquals(6, config.getLongValue1());
    Assertions.assertEquals(7, (long) config.longValueObj);
    Assertions.assertEquals(8, (long) config.getLongValueObj1());

    Assertions.assertEquals(9, config.floatValue, 0);
    Assertions.assertEquals(10, config.getFloatValue1(), 0);
    Assertions.assertEquals(11, config.floatValueObj, 0);
    Assertions.assertEquals(12, config.getFloatValueObj1(), 0);

    Assertions.assertEquals(13, config.doubleValue, 0);
    Assertions.assertEquals(14, config.getDoubleValue1(), 0);
    Assertions.assertEquals(15, config.doubleValueObj, 0);
    Assertions.assertEquals(16, config.getDoubleValueObj1(), 0);

    Assertions.assertTrue(config.booleanValue);
    Assertions.assertTrue(config.isBooleanValue1());
    Assertions.assertTrue(config.booleanValueObj);
    Assertions.assertTrue(config.getBooleanValueObj1());
  }

  @Test
  public void noAnnotation_updateValue() {
    ConfigNoAnnotation config = priorityPropertyManager.createConfigObject(ConfigNoAnnotation.class);

    ArchaiusUtils.setProperty("strValue", "strValue");
    ArchaiusUtils.setProperty("strValue1", "strValue1");

    ArchaiusUtils.setProperty("intValue", 1);
    ArchaiusUtils.setProperty("intValue1", 2);
    ArchaiusUtils.setProperty("intValueObj", 3);
    ArchaiusUtils.setProperty("intValueObj1", 4);

    ArchaiusUtils.setProperty("longValue", 5);
    ArchaiusUtils.setProperty("longValue1", 6);
    ArchaiusUtils.setProperty("longValueObj", 7);
    ArchaiusUtils.setProperty("longValueObj1", 8);

    ArchaiusUtils.setProperty("floatValue", 9.0);
    ArchaiusUtils.setProperty("floatValue1", 10.0);
    ArchaiusUtils.setProperty("floatValueObj", 11.0);
    ArchaiusUtils.setProperty("floatValueObj1", 12.0);

    ArchaiusUtils.setProperty("doubleValue", 13.0);
    ArchaiusUtils.setProperty("doubleValue1", 14.0);
    ArchaiusUtils.setProperty("doubleValueObj", 15.0);
    ArchaiusUtils.setProperty("doubleValueObj1", 16.0);

    ArchaiusUtils.setProperty("booleanValue", true);
    ArchaiusUtils.setProperty("booleanValue1", true);
    ArchaiusUtils.setProperty("booleanValueObj", true);
    ArchaiusUtils.setProperty("booleanValueObj1", true);

    Assertions.assertEquals("strValue", config.strValue);
    Assertions.assertEquals("strValue1", config.getStrValue1());

    Assertions.assertEquals(1, config.intValue);
    Assertions.assertEquals(2, config.getIntValue1());
    Assertions.assertEquals(3, (int) config.intValueObj);
    Assertions.assertEquals(4, (int) config.getIntValueObj1());

    Assertions.assertEquals(5, config.longValue);
    Assertions.assertEquals(6, config.getLongValue1());
    Assertions.assertEquals(7, (long) config.longValueObj);
    Assertions.assertEquals(8, (long) config.getLongValueObj1());

    Assertions.assertEquals(9, config.floatValue, 0);
    Assertions.assertEquals(10, config.getFloatValue1(), 0);
    Assertions.assertEquals(11, config.floatValueObj, 0);
    Assertions.assertEquals(12, config.getFloatValueObj1(), 0);

    Assertions.assertEquals(13, config.doubleValue, 0);
    Assertions.assertEquals(14, config.getDoubleValue1(), 0);
    Assertions.assertEquals(15, config.doubleValueObj, 0);
    Assertions.assertEquals(16, config.getDoubleValueObj1(), 0);

    Assertions.assertTrue(config.booleanValue);
    Assertions.assertTrue(config.isBooleanValue1());
    Assertions.assertTrue(config.booleanValueObj);
    Assertions.assertTrue(config.getBooleanValueObj1());
  }

  @InjectProperties(prefix = "root")
  public static class ConfigWithAnnotation {
    @InjectProperty(prefix = "override", keys = {"high", "low"})
    public String strValue;

    @InjectProperty(keys = "${key}.value")
    public int intValue;

    @InjectProperty(keys = "${low-list}.a.${high-list}.b")
    public long longValue;

    @InjectProperty(keys = "${full-list}")
    public float floatValue;

    @InjectProperty(defaultValue = "abc")
    public String strDef;

    @InjectProperty(defaultValue = "1")
    public int intDef;

    @InjectProperty(defaultValue = "2")
    public long longDef;

    @InjectProperty(defaultValue = "3")
    public float floatDef;

    @InjectProperty(defaultValue = "4")
    public double doubleDef;

    @InjectProperty(defaultValue = "true")
    public boolean booleanDef;
  }

  @Test
  public void annotationDefault() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class);

    Assertions.assertEquals("abc", config.strDef);
    Assertions.assertEquals(1, config.intDef);
    Assertions.assertEquals(2, config.longDef);
    Assertions.assertEquals(3, config.floatDef, 0);
    Assertions.assertEquals(4, config.doubleDef, 0);
    Assertions.assertTrue(config.booleanDef);
  }

  @Test
  public void placeholder_multi_list() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class,
        "low-list", Arrays.asList("low-1", "low-2"),
        "high-list", Arrays.asList("high-1", "high-2"));
    // low-1.a.high-1.b
    // low-1.a.high-2.b
    // low-2.a.high-1.b
    // low-2.a.high-2.b

    Assertions.assertEquals(0, config.longValue);

    ArchaiusUtils.setProperty("root.low-2.a.high-2.b", Long.MAX_VALUE);
    Assertions.assertEquals(Long.MAX_VALUE, config.longValue);

    ArchaiusUtils.setProperty("root.low-2.a.high-1.b", Long.MAX_VALUE - 1);
    Assertions.assertEquals(Long.MAX_VALUE - 1, config.longValue);

    ArchaiusUtils.setProperty("root.low-1.a.high-2.b", Long.MAX_VALUE - 2);
    Assertions.assertEquals(Long.MAX_VALUE - 2, config.longValue);

    ArchaiusUtils.setProperty("root.low-1.a.high-1.b", Long.MAX_VALUE - 3);
    Assertions.assertEquals(Long.MAX_VALUE - 3, config.longValue);
  }

  @Test
  public void placeholder_full_list() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class,
        "full-list", Arrays.asList("l1-1", "l1-2"));

    Assertions.assertEquals(0, config.floatValue, 0);

    ArchaiusUtils.setProperty("root.l1-2", String.valueOf(1f));
    Assertions.assertEquals(1f, config.floatValue, 0);

    ArchaiusUtils.setProperty("root.l1-1", String.valueOf(2f));
    Assertions.assertEquals(2f, config.floatValue, 0);
  }

  @Test
  public void placeholder_normal() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class, "key", "k");

    Assertions.assertEquals(0, config.intValue);

    ArchaiusUtils.setProperty("root.k.value", "1");
    Assertions.assertEquals(1, config.intValue);
  }

  @Test
  public void overridePrefix() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class);

    ArchaiusUtils.setProperty("override.high", "high");
    Assertions.assertEquals("high", config.strValue);

    ArchaiusUtils.setProperty("override.high", null);
    Assertions.assertNull(config.strValue);

    ArchaiusUtils.setProperty("override.low", "low");
    Assertions.assertEquals("low", config.strValue);

    ArchaiusUtils.setProperty("override.low", null);
    Assertions.assertNull(config.strValue);
  }

  @InjectProperties(prefix = "root")
  public static class ConfigWithPojo {

    @InjectProperties(prefix = "root.model")
    public static class Model {
      @InjectProperty(defaultValue = "h")
      public String name;
    }

    @InjectProperty(defaultValue = "4")
    public int index;

    public Model model;
  }

  @Test
  public void configWithPojoWork() {
    ConfigWithPojo config = priorityPropertyManager.createConfigObject(ConfigWithPojo.class);
    ConfigWithPojo.Model model = priorityPropertyManager.createConfigObject(ConfigWithPojo.Model.class);
    config.model = model;

    Assertions.assertEquals(4, config.index);
    Assertions.assertEquals("h", config.model.name);
    ArchaiusUtils.setProperty("root.index", "5");
    ArchaiusUtils.setProperty("root.model.name", "w");
    Assertions.assertEquals(5, config.index);
    Assertions.assertEquals("w", config.model.name);
  }
}
