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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TestConfigMapping {
  @Test
  public void testMapping() {
    List<String> value = ConfigMapping.map("SERVICECOMB_ENV");
    assertEquals(2, value.size());
    assertEquals("service_description.environment", value.get(0));
    assertEquals("service_description.environment.old", value.get(1));

    Map<String, Object> m = ConfigMapping.getMapping();
    assertNotNull(m);
  }

  @Test
  public void testConvertedMap() {
    String value = ConfigMapping.map("CSE_ENV_MAPPING");
    Map<String, Object> m = ConfigMapping.getMapping();
    Map<String, Object> m1 = ConfigMapping.getConvertedMap(m);
    assertEquals("servicecomb.testmapping.key", value);
    assertNotNull(m1);
  }
}
