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

package org.apache.servicecomb.config;

import static com.seanyinx.github.unit.scaffolding.Randomness.nextBoolean;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

import java.util.HashMap;
import java.util.Objects;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.seanyinx.github.unit.scaffolding.Poller;
import com.seanyinx.github.unit.scaffolding.Randomness;

public class DynamicPropertiesTest {
  private static final String stringPropertyName = uniquify("stringPropertyName");

  private static final String intPropertyName = uniquify("intPropertyName");

  private static final String longPropertyName = uniquify("longPropertyName");

  private static final String floatPropertyName = uniquify("floatPropertyName");

  private static final String doublePropertyName = uniquify("doublePropertyName");

  private static final String booleanPropertyName = uniquify("booleanPropertyName");

  private static final String stringOldValue = uniquify("stringPropertyValue");

  private static final int intOldValue = Randomness.nextInt();

  private static final long longOldValue = Randomness.nextLong();

  private static final float floatOldValue = Double.valueOf(Randomness.nextDouble()).floatValue();

  private static final double doubleOldValue = Randomness.nextDouble();

  private static final boolean booleanOldValue = nextBoolean();

  private static final double ERROR = 0.0000001;

  private static Environment environment;

  private static DynamicProperties dynamicProperties;

  private final Poller poller = new Poller(2000, 100);

  private volatile String stringPropertyValue;

  private volatile int intPropertyValue;

  private volatile long longPropertyValue;

  private volatile double floatPropertyValue;

  private volatile double doublePropertyValue;

  private volatile boolean booleanPropertyValue;


  @BeforeClass
  public static void setUpClass() throws Exception {
    environment = Mockito.mock(Environment.class);
    writeInitialConfig();
    dynamicProperties = new DynamicPropertiesImpl(environment);
  }

  @AfterClass
  public static void tearDown() throws Exception {

  }

  private static void writeInitialConfig() throws Exception {
    Mockito.when(environment.getProperty(stringPropertyName, (String) null)).thenReturn(stringOldValue);
    Mockito.when(environment.getProperty(intPropertyName, int.class, 0)).thenReturn(intOldValue);
    Mockito.when(environment.getProperty(longPropertyName, long.class, 0L)).thenReturn(longOldValue);
    Mockito.when(environment.getProperty(floatPropertyName, float.class, 0F)).thenReturn(floatOldValue);
    Mockito.when(environment.getProperty(doublePropertyName, double.class, 0D)).thenReturn(doubleOldValue);
    Mockito.when(environment.getProperty(booleanPropertyName, boolean.class, false))
        .thenReturn(booleanOldValue);
  }

  @Test
  public void observesSpecifiedStringProperty() throws Exception {
    String property = dynamicProperties.getStringProperty(stringPropertyName, null);
    assertThat(property, is(stringOldValue));

    property = dynamicProperties.getStringProperty(stringPropertyName, value -> stringPropertyValue = value, null);
    assertThat(property, is(stringOldValue));

    String newValue = uniquify("newValue");

    Mockito.when(environment.getProperty(stringPropertyName, stringOldValue)).thenReturn(newValue);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(stringPropertyName, newValue);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    poller.assertEventually(() -> Objects.equals(stringPropertyValue, newValue));
  }

  @Test
  public void observesSpecifiedIntProperty() throws Exception {
    int property = dynamicProperties.getIntProperty(intPropertyName, 0);
    assertThat(property, is(intOldValue));

    property = dynamicProperties.getIntProperty(intPropertyName, value -> intPropertyValue = value, 0);
    assertThat(property, is(intOldValue));

    int newValue = Randomness.nextInt();

    Mockito.when(environment.getProperty(intPropertyName, int.class, intOldValue)).thenReturn(newValue);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(intPropertyName, newValue);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    poller.assertEventually(() -> intPropertyValue == newValue);
  }

  @Test
  public void observesSpecifiedLongProperty() throws Exception {
    long property = dynamicProperties.getLongProperty(longPropertyName, 0);
    assertThat(property, is(longOldValue));

    property = dynamicProperties.getLongProperty(longPropertyName, value -> longPropertyValue = value, 0);
    assertThat(property, is(longOldValue));

    long newValue = Randomness.nextLong();

    Mockito.when(environment.getProperty(longPropertyName, long.class, longOldValue)).thenReturn(newValue);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(longPropertyName, newValue);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    poller.assertEventually(() -> longPropertyValue == newValue);
  }

  @Test
  public void observesSpecifiedFloatProperty() throws Exception {
    double property = dynamicProperties.getFloatProperty(floatPropertyName, 0);
    assertThat(property, closeTo(floatOldValue, ERROR));

    property = dynamicProperties.getFloatProperty(floatPropertyName, value -> floatPropertyValue = value, 0);
    assertThat(property, closeTo(floatOldValue, ERROR));

    float newValue = Double.valueOf(Randomness.nextDouble()).floatValue();

    Mockito.when(environment.getProperty(floatPropertyName, float.class, floatOldValue)).thenReturn(newValue);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(floatPropertyName, newValue);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    poller.assertEventually(() -> Math.abs(floatPropertyValue - newValue) < ERROR);
  }

  @Test
  public void observesSpecifiedDoubleProperty() throws Exception {
    double property = dynamicProperties.getDoubleProperty(doublePropertyName, 0);
    assertThat(property, closeTo(doubleOldValue, ERROR));

    property = dynamicProperties.getDoubleProperty(doublePropertyName, value -> doublePropertyValue = value, 0);
    assertThat(property, closeTo(doubleOldValue, ERROR));

    double newValue = Randomness.nextDouble();

    Mockito.when(environment.getProperty(doublePropertyName, double.class, doubleOldValue)).thenReturn(newValue);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(doublePropertyName, newValue);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    poller.assertEventually(() -> Math.abs(doublePropertyValue - newValue) < ERROR);
  }

  @Test
  public void observesSpecifiedBooleanProperty() throws Exception {
    boolean property = dynamicProperties.getBooleanProperty(booleanPropertyName, false);
    assertThat(property, is(booleanOldValue));

    property = dynamicProperties.getBooleanProperty(
        booleanPropertyName,
        value -> booleanPropertyValue = value,
        false);
    assertThat(property, is(booleanOldValue));

    boolean newValue = !booleanOldValue;

    Mockito.when(environment.getProperty(booleanPropertyName, boolean.class, booleanOldValue)).thenReturn(newValue);
    HashMap<String, Object> updated = new HashMap<>();
    updated.put(booleanPropertyName, newValue);
    EventManager.post(ConfigurationChangedEvent.createIncremental(updated));

    poller.assertEventually(() -> booleanPropertyValue == newValue);
  }
}
