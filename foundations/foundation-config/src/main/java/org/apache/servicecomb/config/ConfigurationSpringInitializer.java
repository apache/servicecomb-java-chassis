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

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.AbstractConfiguration;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;

public class ConfigurationSpringInitializer extends PropertyPlaceholderConfigurer {
  public ConfigurationSpringInitializer() {
    ConfigUtil.installDynamicConfig();
    setOrder(Ordered.LOWEST_PRECEDENCE / 2);
    setIgnoreUnresolvablePlaceholders(true);
  }

  @Override
  protected Properties mergeProperties() throws IOException {
    Properties properties = super.mergeProperties();

    AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    Iterator<String> iter = config.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      Object value = config.getProperty(key);
      properties.put(key, value);
    }
    return properties;
  }

  @Override
  protected String resolvePlaceholder(String placeholder, Properties props) {
    String propertyValue = super.resolvePlaceholder(placeholder, props);
    if (propertyValue == null) {
      return DynamicPropertyFactory.getInstance().getStringProperty(placeholder, null).get();
    }
    return propertyValue;
  }
}
