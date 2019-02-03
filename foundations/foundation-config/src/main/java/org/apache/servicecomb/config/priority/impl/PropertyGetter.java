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

package org.apache.servicecomb.config.priority.impl;

import com.netflix.config.DynamicStringProperty;
import com.netflix.config.Property;
import com.netflix.config.PropertyWrapper;

public interface PropertyGetter<T> {
  Property<T> getProperty(String propName, T defaultValue);

  static Property<String> getStringProperty(String propName, String defaultValue) {
    return new DynamicStringProperty(propName, defaultValue);
  }

  class DynamicLongProperty extends PropertyWrapper<Long> {
    public DynamicLongProperty(String propName, Long defaultValue) {
      super(propName, defaultValue);
    }

    public Long get() {
      return prop.getLong(defaultValue);
    }

    @Override
    public Long getValue() {
      return get();
    }
  }

  static Property<Long> getLongProperty(String propName, Long defaultValue) {
    return new DynamicLongProperty(propName, defaultValue);
  }

  class DynamicIntegerProperty extends PropertyWrapper<Integer> {
    public DynamicIntegerProperty(String propName, Integer defaultValue) {
      super(propName, defaultValue);
    }

    public Integer get() {
      return prop.getInteger(defaultValue);
    }

    @Override
    public Integer getValue() {
      return get();
    }
  }

  static Property<Integer> getIntProperty(String propName, Integer defaultValue) {
    return new DynamicIntegerProperty(propName, defaultValue);
  }

  class DynamicBooleanProperty extends PropertyWrapper<Boolean> {
    public DynamicBooleanProperty(String propName, Boolean defaultValue) {
      super(propName, defaultValue);
    }

    public Boolean get() {
      return prop.getBoolean(defaultValue);
    }

    @Override
    public Boolean getValue() {
      return get();
    }
  }

  static Property<Boolean> getBooleanProperty(String propName, Boolean defaultValue) {
    return new DynamicBooleanProperty(propName, defaultValue);
  }

  class DynamicDoubleProperty extends PropertyWrapper<Double> {
    public DynamicDoubleProperty(String propName, Double defaultValue) {
      super(propName, defaultValue);
    }

    public Double get() {
      return prop.getDouble(defaultValue);
    }

    @Override
    public Double getValue() {
      return get();
    }
  }

  static Property<Double> getDoubleProperty(String propName, Double defaultValue) {
    return new DynamicDoubleProperty(propName, defaultValue);
  }

  class DynamicFloatProperty extends PropertyWrapper<Float> {
    public DynamicFloatProperty(String propName, Float defaultValue) {
      super(propName, defaultValue);
    }

    public Float get() {
      return prop.getFloat(defaultValue);
    }

    @Override
    public Float getValue() {
      return get();
    }
  }

  static Property<Float> getFloatProperty(String propName, Float defaultValue) {
    return new DynamicFloatProperty(propName, defaultValue);
  }
}