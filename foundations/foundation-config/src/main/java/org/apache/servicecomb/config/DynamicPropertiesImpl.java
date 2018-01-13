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

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import org.apache.commons.configuration.AbstractConfiguration;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicDoubleProperty;
import com.netflix.config.DynamicFloatProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

class DynamicPropertiesImpl implements DynamicProperties {

  DynamicPropertiesImpl() {
  }

  DynamicPropertiesImpl(AbstractConfiguration... configurations) {
    ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();
    Arrays.stream(configurations).forEach(configuration::addConfiguration);

    ConfigurationManager.install(configuration);
  }

  @Override
  public String getStringProperty(String propertyName, Consumer<String> consumer, String defaultValue) {
    DynamicStringProperty prop = propertyFactoryInstance().getStringProperty(propertyName, defaultValue);
    prop.addCallback(() -> consumer.accept(prop.get()));
    return prop.get();
  }

  @Override
  public String getStringProperty(String propertyName, String defaultValue) {
    return propertyFactoryInstance().getStringProperty(propertyName, defaultValue).get();
  }

  @Override
  public int getIntProperty(String propertyName, IntConsumer consumer, int defaultValue) {
    DynamicIntProperty prop = propertyFactoryInstance().getIntProperty(propertyName, defaultValue);
    prop.addCallback(() -> consumer.accept(prop.get()));
    return prop.get();
  }

  @Override
  public int getIntProperty(String propertyName, int defaultValue) {
    return propertyFactoryInstance().getIntProperty(propertyName, defaultValue).get();
  }

  @Override
  public long getLongProperty(String propertyName, LongConsumer consumer, long defaultValue) {
    DynamicLongProperty prop = propertyFactoryInstance().getLongProperty(propertyName, defaultValue);
    prop.addCallback(() -> consumer.accept(prop.get()));
    return prop.get();
  }

  @Override
  public long getLongProperty(String propertyName, long defaultValue) {
    return propertyFactoryInstance().getLongProperty(propertyName, defaultValue).get();
  }

  @Override
  public float getFloatProperty(String propertyName, DoubleConsumer consumer, float defaultValue) {
    DynamicFloatProperty prop = propertyFactoryInstance().getFloatProperty(propertyName, defaultValue);
    prop.addCallback(() -> consumer.accept(prop.get()));
    return prop.get();
  }

  @Override
  public float getFloatProperty(String propertyName, float defaultValue) {
    return propertyFactoryInstance().getFloatProperty(propertyName, defaultValue).get();
  }

  @Override
  public double getDoubleProperty(String propertyName, DoubleConsumer consumer, double defaultValue) {
    DynamicDoubleProperty prop = propertyFactoryInstance().getDoubleProperty(propertyName, defaultValue);
    prop.addCallback(() -> consumer.accept(prop.get()));
    return prop.get();
  }

  @Override
  public double getDoubleProperty(String propertyName, double defaultValue) {
    return propertyFactoryInstance().getDoubleProperty(propertyName, defaultValue).get();
  }

  @Override
  public boolean getBooleanProperty(String propertyName, Consumer<Boolean> consumer, boolean defaultValue) {
    DynamicBooleanProperty prop = propertyFactoryInstance().getBooleanProperty(propertyName, defaultValue);
    prop.addCallback(() -> consumer.accept(prop.get()));
    return prop.get();
  }

  @Override
  public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
    return propertyFactoryInstance().getBooleanProperty(propertyName, defaultValue).get();
  }

  private DynamicPropertyFactory propertyFactoryInstance() {
    return DynamicPropertyFactory.getInstance();
  }
}
