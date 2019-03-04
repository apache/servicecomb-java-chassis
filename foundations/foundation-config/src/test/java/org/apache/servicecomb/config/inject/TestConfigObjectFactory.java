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

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestConfigObjectFactory {
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

  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void noAnnotation_defaultValue() {
    ConfigNoAnnotation config = new ConfigObjectFactory().create(ConfigNoAnnotation.class);

    Assert.assertNull(config.strValue);
    Assert.assertNull(config.getStrValue1());

    Assert.assertEquals(0, config.intValue);
    Assert.assertEquals(0, config.getIntValue1());
    Assert.assertNull(config.intValueObj);
    Assert.assertNull(config.getIntValueObj1());

    Assert.assertEquals(0, config.longValue);
    Assert.assertEquals(0, config.getLongValue1());
    Assert.assertNull(config.longValueObj);
    Assert.assertNull(config.getLongValueObj1());

    Assert.assertEquals(0, config.floatValue, 0);
    Assert.assertEquals(0, config.getFloatValue1(), 0);
    Assert.assertNull(config.floatValueObj);
    Assert.assertNull(config.getFloatValueObj1());

    Assert.assertEquals(0, config.doubleValue, 0);
    Assert.assertEquals(0, config.getDoubleValue1(), 0);
    Assert.assertNull(config.doubleValueObj);
    Assert.assertNull(config.getDoubleValueObj1());

    Assert.assertFalse(config.booleanValue);
    Assert.assertFalse(config.isBooleanValue1());
    Assert.assertNull(config.booleanValueObj);
    Assert.assertNull(config.getBooleanValueObj1());
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

    ConfigNoAnnotation config = new ConfigObjectFactory().create(ConfigNoAnnotation.class);

    Assert.assertEquals("strValue", config.strValue);
    Assert.assertEquals("strValue1", config.getStrValue1());

    Assert.assertEquals(1, config.intValue);
    Assert.assertEquals(2, config.getIntValue1());
    Assert.assertEquals(3, (int) config.intValueObj);
    Assert.assertEquals(4, (int) config.getIntValueObj1());

    Assert.assertEquals(5, config.longValue);
    Assert.assertEquals(6, config.getLongValue1());
    Assert.assertEquals(7, (long) config.longValueObj);
    Assert.assertEquals(8, (long) config.getLongValueObj1());

    Assert.assertEquals(9, config.floatValue, 0);
    Assert.assertEquals(10, config.getFloatValue1(), 0);
    Assert.assertEquals(11, config.floatValueObj, 0);
    Assert.assertEquals(12, config.getFloatValueObj1(), 0);

    Assert.assertEquals(13, config.doubleValue, 0);
    Assert.assertEquals(14, config.getDoubleValue1(), 0);
    Assert.assertEquals(15, config.doubleValueObj, 0);
    Assert.assertEquals(16, config.getDoubleValueObj1(), 0);

    Assert.assertTrue(config.booleanValue);
    Assert.assertTrue(config.isBooleanValue1());
    Assert.assertTrue(config.booleanValueObj);
    Assert.assertTrue(config.getBooleanValueObj1());
  }

  @Test
  public void noAnnotation_updateValue() {
    ConfigNoAnnotation config = new ConfigObjectFactory().create(ConfigNoAnnotation.class);

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

    Assert.assertEquals("strValue", config.strValue);
    Assert.assertEquals("strValue1", config.getStrValue1());

    Assert.assertEquals(1, config.intValue);
    Assert.assertEquals(2, config.getIntValue1());
    Assert.assertEquals(3, (int) config.intValueObj);
    Assert.assertEquals(4, (int) config.getIntValueObj1());

    Assert.assertEquals(5, config.longValue);
    Assert.assertEquals(6, config.getLongValue1());
    Assert.assertEquals(7, (long) config.longValueObj);
    Assert.assertEquals(8, (long) config.getLongValueObj1());

    Assert.assertEquals(9, config.floatValue, 0);
    Assert.assertEquals(10, config.getFloatValue1(), 0);
    Assert.assertEquals(11, config.floatValueObj, 0);
    Assert.assertEquals(12, config.getFloatValueObj1(), 0);

    Assert.assertEquals(13, config.doubleValue, 0);
    Assert.assertEquals(14, config.getDoubleValue1(), 0);
    Assert.assertEquals(15, config.doubleValueObj, 0);
    Assert.assertEquals(16, config.getDoubleValueObj1(), 0);

    Assert.assertTrue(config.booleanValue);
    Assert.assertTrue(config.isBooleanValue1());
    Assert.assertTrue(config.booleanValueObj);
    Assert.assertTrue(config.getBooleanValueObj1());
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
    ConfigWithAnnotation config = new ConfigObjectFactory().create(ConfigWithAnnotation.class);

    Assert.assertEquals("abc", config.strDef);
    Assert.assertEquals(1, config.intDef);
    Assert.assertEquals(2, config.longDef);
    Assert.assertEquals(3, config.floatDef, 0);
    Assert.assertEquals(4, config.doubleDef, 0);
    Assert.assertTrue(config.booleanDef);
  }

  @Test
  public void placeholder_multi_list() {
    ConfigWithAnnotation config = new ConfigObjectFactory().create(ConfigWithAnnotation.class,
        "low-list", Arrays.asList("low-1", "low-2"),
        "high-list", Arrays.asList("high-1", "high-2"));
    // low-1.a.high-1.b
    // low-1.a.high-2.b
    // low-2.a.high-1.b
    // low-2.a.high-2.b

    Assert.assertEquals(0, config.longValue);

    ArchaiusUtils.setProperty("root.low-2.a.high-2.b", Long.MAX_VALUE);
    Assert.assertEquals(Long.MAX_VALUE, config.longValue);

    ArchaiusUtils.setProperty("root.low-2.a.high-1.b", Long.MAX_VALUE - 1);
    Assert.assertEquals(Long.MAX_VALUE - 1, config.longValue);

    ArchaiusUtils.setProperty("root.low-1.a.high-2.b", Long.MAX_VALUE - 2);
    Assert.assertEquals(Long.MAX_VALUE - 2, config.longValue);

    ArchaiusUtils.setProperty("root.low-1.a.high-1.b", Long.MAX_VALUE - 3);
    Assert.assertEquals(Long.MAX_VALUE - 3, config.longValue);
  }

  @Test
  public void placeholder_full_list() {
    ConfigWithAnnotation config = new ConfigObjectFactory().create(ConfigWithAnnotation.class,
        "full-list", Arrays.asList("l1-1", "l1-2"));

    Assert.assertEquals(0, config.floatValue, 0);

    ArchaiusUtils.setProperty("root.l1-2", String.valueOf(1f));
    Assert.assertEquals(1f, config.floatValue, 0);

    ArchaiusUtils.setProperty("root.l1-1", String.valueOf(2f));
    Assert.assertEquals(2f, config.floatValue, 0);
  }

  @Test
  public void placeholder_normal() {
    ConfigWithAnnotation config = new ConfigObjectFactory().create(ConfigWithAnnotation.class, "key", "k");

    Assert.assertEquals(0, config.intValue);

    ArchaiusUtils.setProperty("root.k.value", "1");
    Assert.assertEquals(1, config.intValue);
  }

  @Test
  public void overridePrefix() {
    ConfigWithAnnotation config = new ConfigObjectFactory().create(ConfigWithAnnotation.class);

    ArchaiusUtils.setProperty("override.high", "high");
    Assert.assertEquals("high", config.strValue);

    ArchaiusUtils.updateProperty("override.high", null);
    Assert.assertNull(config.strValue);

    ArchaiusUtils.setProperty("override.low", "low");
    Assert.assertEquals("low", config.strValue);

    ArchaiusUtils.updateProperty("override.low", null);
    Assert.assertNull(config.strValue);
  }
}
