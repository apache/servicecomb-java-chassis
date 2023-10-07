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

import static org.apache.servicecomb.foundation.test.scaffolding.AssertUtils.assertPrettyJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class BootStrapPropertiesTest {
  private Map<String, String> readInstanceProperties(String yaml) {
    Map<String, Object> properties = YAMLUtil.yaml2Properties(yaml);
    ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    EnumerablePropertySource propertySource = new MapPropertySource("yaml", properties);
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);

    List<String> keys = new ArrayList<>();
    for (Entry<String, Object> entry : properties.entrySet()) {
      Mockito.when(environment.getProperty(entry.getKey())).thenReturn(entry.getValue().toString());
      keys.add(entry.getKey());
    }

    return BootStrapProperties.readServiceInstanceProperties(environment);
  }

  @Test
  void should_be_empty_when_old_and_new_key_not_exists() {
    Map<String, String> properties = readInstanceProperties("k: v");

    assertThat(properties).isEmpty();
  }

  @Test
  void should_read_boolean_to_string() {
    Map<String, String> properties = readInstanceProperties(""
        + "servicecomb:\n"
        + "  instance:\n"
        + "    properties:\n"
        + "      k: true");

    assertPrettyJson(properties).isEqualTo(""
        + "{\n"
        + "  \"k\" : \"true\"\n"
        + "}");
  }

  @Test
  void should_read_number_to_string() {
    Map<String, String> properties = readInstanceProperties(""
        + "servicecomb:\n"
        + "  instance:\n"
        + "    properties:\n"
        + "      k: 1");

    assertPrettyJson(properties).isEqualTo(""
        + "{\n"
        + "  \"k\" : \"1\"\n"
        + "}");
  }

  @Test
  void should_read_by_old_prefix_when_new_prefix_not_exists() {
    Map<String, String> properties = readInstanceProperties(""
        + "instance_description:\n"
        + "  properties:\n"
        + "    k: v\n"
        + "    k1: v1");

    assertPrettyJson(properties).isEqualTo(""
        + "{\n"
        + "  \"k1\" : \"v1\",\n"
        + "  \"k\" : \"v\"\n"
        + "}");
  }

  @Test
  void should_ignore_old_prefix_when_new_prefix_exists() {
    Map<String, String> properties = readInstanceProperties(""
        + "instance_description:\n"
        + "  properties:\n"
        + "    k: v\n"
        + "    k1: v1\n"
        + "servicecomb:\n"
        + "  instance:\n"
        + "    properties:\n"
        + "      k: new");

    assertPrettyJson(properties).isEqualTo(""
        + "{\n"
        + "  \"k\" : \"new\"\n"
        + "}");
  }
}
