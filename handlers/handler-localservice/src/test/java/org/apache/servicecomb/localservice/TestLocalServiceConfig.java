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

package org.apache.servicecomb.localservice;

import java.util.Map;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class TestLocalServiceConfig {
  private final MockEnvironment environment = new MockEnvironment();

  @BeforeEach
  public void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
  }

  @AfterEach
  public void tearDown() {
    LegacyPropertyFactory.setEnvironment(null);
  }

  @Test
  public void listenerDisabledByDefault() {
    Assertions.assertFalse(LocalServiceConfig.isEnabled());
  }

  @Test
  public void listenerEnabled() {
    environment.setProperty(LocalServiceConfig.KEY_ENABLED, "true");
    Assertions.assertTrue(LocalServiceConfig.isEnabled());
  }

  @Test
  public void filterEnabledByDefault() {
    Assertions.assertTrue(LocalServiceConfig.isFilterEnabled());
  }

  @Test
  public void filterCanBeDisabled() {
    environment.setProperty(LocalServiceConfig.KEY_FILTER_ENABLED, "false");
    Assertions.assertFalse(LocalServiceConfig.isFilterEnabled());
  }

  @Test
  public void filterRejectsInvalidValue() {
    environment.setProperty(LocalServiceConfig.KEY_FILTER_ENABLED, "not-a-boolean");
    Assertions.assertThrows(Exception.class, LocalServiceConfig::isFilterEnabled);
  }

  @Test
  public void keysAreStable() {
    Map.of(
        LocalServiceConfig.KEY_ENABLED, "servicecomb.localservice.enabled",
        LocalServiceConfig.KEY_FILTER_ENABLED, "servicecomb.loadbalance.filter.localService.enabled")
        .forEach((key, expected) -> Assertions.assertEquals(expected, key));
  }
}
