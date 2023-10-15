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
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.config.priority.TestPriorityPropertyBase;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    Mockito.when(environment.getProperty("strValue")).thenReturn("strValue");
    Mockito.when(environment.getProperty("strValue1")).thenReturn("strValue1");

    Mockito.when(environment.getProperty("intValue", Integer.class)).thenReturn(1);
    Mockito.when(environment.getProperty("intValue1", Integer.class)).thenReturn(2);
    Mockito.when(environment.getProperty("intValueObj", Integer.class)).thenReturn(3);
    Mockito.when(environment.getProperty("intValueObj1", Integer.class)).thenReturn(4);

    Mockito.when(environment.getProperty("longValue", Long.class)).thenReturn(5L);
    Mockito.when(environment.getProperty("longValue1", Long.class)).thenReturn(6L);
    Mockito.when(environment.getProperty("longValueObj", Long.class)).thenReturn(7L);
    Mockito.when(environment.getProperty("longValueObj1", Long.class)).thenReturn(8L);

    Mockito.when(environment.getProperty("floatValue", Float.class)).thenReturn(9.0F);
    Mockito.when(environment.getProperty("floatValue1", Float.class)).thenReturn(10.0F);
    Mockito.when(environment.getProperty("floatValueObj", Float.class)).thenReturn(11.0F);
    Mockito.when(environment.getProperty("floatValueObj1", Float.class)).thenReturn(12.0F);

    Mockito.when(environment.getProperty("doubleValue", Double.class)).thenReturn(13.0D);
    Mockito.when(environment.getProperty("doubleValue1", Double.class)).thenReturn(14.0D);
    Mockito.when(environment.getProperty("doubleValueObj", Double.class)).thenReturn(15.0D);
    Mockito.when(environment.getProperty("doubleValueObj1", Double.class)).thenReturn(16.0D);

    Mockito.when(environment.getProperty("booleanValue", Boolean.class)).thenReturn(true);
    Mockito.when(environment.getProperty("booleanValue1", Boolean.class)).thenReturn(true);
    Mockito.when(environment.getProperty("booleanValueObj", Boolean.class)).thenReturn(true);
    Mockito.when(environment.getProperty("booleanValueObj1", Boolean.class)).thenReturn(true);

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

    Mockito.when(environment.getProperty("strValue")).thenReturn("strValue");
    Mockito.when(environment.getProperty("strValue1")).thenReturn("strValue1");

    Mockito.when(environment.getProperty("intValue", Integer.class)).thenReturn(1);
    Mockito.when(environment.getProperty("intValue1", Integer.class)).thenReturn(2);
    Mockito.when(environment.getProperty("intValueObj", Integer.class)).thenReturn(3);
    Mockito.when(environment.getProperty("intValueObj1", Integer.class)).thenReturn(4);

    Mockito.when(environment.getProperty("longValue", Long.class)).thenReturn(5L);
    Mockito.when(environment.getProperty("longValue1", Long.class)).thenReturn(6L);
    Mockito.when(environment.getProperty("longValueObj", Long.class)).thenReturn(7L);
    Mockito.when(environment.getProperty("longValueObj1", Long.class)).thenReturn(8L);

    Mockito.when(environment.getProperty("floatValue", Float.class)).thenReturn(9.0F);
    Mockito.when(environment.getProperty("floatValue1", Float.class)).thenReturn(10.0F);
    Mockito.when(environment.getProperty("floatValueObj", Float.class)).thenReturn(11.0F);
    Mockito.when(environment.getProperty("floatValueObj1", Float.class)).thenReturn(12.0F);

    Mockito.when(environment.getProperty("doubleValue", Double.class)).thenReturn(13.0D);
    Mockito.when(environment.getProperty("doubleValue1", Double.class)).thenReturn(14.0D);
    Mockito.when(environment.getProperty("doubleValueObj", Double.class)).thenReturn(15.0D);
    Mockito.when(environment.getProperty("doubleValueObj1", Double.class)).thenReturn(16.0D);

    Mockito.when(environment.getProperty("booleanValue", Boolean.class)).thenReturn(true);
    Mockito.when(environment.getProperty("booleanValue1", Boolean.class)).thenReturn(true);
    Mockito.when(environment.getProperty("booleanValueObj", Boolean.class)).thenReturn(true);
    Mockito.when(environment.getProperty("booleanValueObj1", Boolean.class)).thenReturn(true);

    HashMap<String, Object> updated = new HashMap<>();
    updated.put("strValue", "strValue");
    updated.put("strValue1", "strValue1");

    updated.put("intValue", 0);
    updated.put("intValue1", 1);
    updated.put("intValueObj", 2);
    updated.put("intValueObj1", 3);

    updated.put("longValue", 5L);
    updated.put("longValue1", 6L);
    updated.put("longValueObj", 7L);
    updated.put("longValueObj1", 8L);

    updated.put("floatValue", 9.0F);
    updated.put("floatValue1", 10.0F);
    updated.put("floatValueObj", 11.0F);
    updated.put("floatValueObj1", 12.0F);

    updated.put("doubleValue", 13.0D);
    updated.put("doubleValue1", 14.0D);
    updated.put("doubleValueObj", 15.0D);
    updated.put("doubleValueObj1", 16.0D);

    updated.put("booleanValue", true);
    updated.put("booleanValue1", true);
    updated.put("booleanValueObj", true);
    updated.put("booleanValueObj1", true);

    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

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

    Mockito.when(environment.getProperty("root.low-2.a.high-2.b", Long.class)).thenReturn(Long.MAX_VALUE);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put("root.low-2.a.high-2.b", Long.MAX_VALUE);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertEquals(Long.MAX_VALUE, config.longValue);

    Mockito.when(environment.getProperty("root.low-2.a.high-1.b", Long.class)).thenReturn(Long.MAX_VALUE - 1);
    updated = new HashMap<>();
    updated.put("root.low-2.a.high-1.b", Long.MAX_VALUE - 1);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    Assertions.assertEquals(Long.MAX_VALUE - 1, config.longValue);

    Mockito.when(environment.getProperty("root.low-1.a.high-2.b", Long.class)).thenReturn(Long.MAX_VALUE - 2);
    updated = new HashMap<>();
    updated.put("root.low-1.a.high-2.b", Long.MAX_VALUE - 2);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertEquals(Long.MAX_VALUE - 2, config.longValue);

    Mockito.when(environment.getProperty("root.low-1.a.high-1.b", Long.class)).thenReturn(Long.MAX_VALUE - 3);
    updated = new HashMap<>();
    updated.put("root.low-1.a.high-1.b", Long.MAX_VALUE - 3);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertEquals(Long.MAX_VALUE - 3, config.longValue);
  }

  @Test
  public void placeholder_full_list() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class,
        "full-list", Arrays.asList("l1-1", "l1-2"));

    Assertions.assertEquals(0, config.floatValue, 0);

    Mockito.when(environment.getProperty("root.l1-2", Float.class)).thenReturn(1F);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put("root.l1-2", 1F);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    Assertions.assertEquals(1F, config.floatValue, 0);

    Mockito.when(environment.getProperty("root.l1-1", Float.class)).thenReturn(2F);
    updated = new HashMap<>();
    updated.put("root.l1-1", 2F);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    Assertions.assertEquals(2F, config.floatValue, 0);
  }

  @Test
  public void placeholder_normal() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class, "key", "k");

    Assertions.assertEquals(0, config.intValue);

    Mockito.when(environment.getProperty("root.k.value", Integer.class)).thenReturn(1);
    Map<String, Object> updated = new HashMap<>();
    updated.put("root.k.value", 1);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertEquals(1, config.intValue);
  }

  @Test
  public void overridePrefix() {
    ConfigWithAnnotation config = priorityPropertyManager.createConfigObject(ConfigWithAnnotation.class);

    Mockito.when(environment.getProperty("override.high")).thenReturn("high");
    HashMap<String, Object> updated = new HashMap<>();
    updated.put("override.high", "high");
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertEquals("high", config.strValue);

    Mockito.when(environment.getProperty("override.high")).thenReturn(null);
    updated = new HashMap<>();
    updated.put("override.high", null);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertNull(config.strValue);

    Mockito.when(environment.getProperty("override.low")).thenReturn("low");
    updated = new HashMap<>();
    updated.put("override.low", "low");
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
    Assertions.assertEquals("low", config.strValue);

    Mockito.when(environment.getProperty("override.low")).thenReturn(null);
    updated = new HashMap<>();
    updated.put("override.low", null);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));
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

    Mockito.when(environment.getProperty("root.index", Integer.class)).thenReturn(5);
    Mockito.when(environment.getProperty("root.model.name")).thenReturn("w");
    Map<String, Object> updated = new HashMap<>();
    updated.put("root.index", 5);
    updated.put("root.model.name", "w");
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    Assertions.assertEquals(5, config.index);
    Assertions.assertEquals("w", config.model.name);
  }
}
