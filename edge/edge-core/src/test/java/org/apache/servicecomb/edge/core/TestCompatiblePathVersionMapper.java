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

package org.apache.servicecomb.edge.core;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.registry.version.VersionRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCompatiblePathVersionMapper {
  CompatiblePathVersionMapper mapper = new CompatiblePathVersionMapper();

  @Test
  public void getOrCreate() {
    VersionRule versionRule = mapper.getOrCreate("v1");

    Assertions.assertEquals("1.0.0.0-2.0.0.0", versionRule.getVersionRule());
  }

  @Test
  public void createVersionRule_empty() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> mapper.getOrCreate(""));
    Assertions.assertEquals("pathVersion \"\" is invalid, format must be v+number or V+number.", exception.getMessage());
  }

  @Test
  public void createVersionRule_invalidFormat() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> mapper.getOrCreate("a1"));
    Assertions.assertEquals("pathVersion \"a1\" is invalid, format must be v+number or V+number.", exception.getMessage());
  }

  @Test
  public void createVersionRule_invalidNumber() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> mapper.getOrCreate("va"));
    Assertions.assertEquals("pathVersion \"va\" is invalid, format must be v+number or V+number.", exception.getMessage());
  }

  @Test
  public void createVersionRule_tooSmall() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> mapper.getOrCreate("v-1"));
    Assertions.assertEquals("pathVersion \"v-1\" is invalid, version range is [0, 32767].", exception.getMessage());
  }

  @Test
  public void createVersionRule_tooBig() {
    ServiceCombException exception = Assertions.assertThrows(ServiceCombException.class,
            () -> mapper.getOrCreate("v32768"));
    Assertions.assertEquals("pathVersion \"v32768\" is invalid, version range is [0, 32767].", exception.getMessage());
  }

  @Test
  public void createVersionRule_32767() {
    VersionRule versionRule = mapper.getOrCreate("v32767");

    Assertions.assertEquals("32767.0.0.0+", versionRule.getVersionRule());
  }
}
