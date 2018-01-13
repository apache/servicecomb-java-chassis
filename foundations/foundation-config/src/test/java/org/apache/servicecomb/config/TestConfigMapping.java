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

import java.util.Map;

import org.junit.Test;

/**
 * Created by   on 2017/1/5.
 */
public class TestConfigMapping {
  @Test
  public void testMapping() {
    String value = ConfigMapping.map("eureka.client.serviceUrl.defaultZone");
    Map<String, Object> m = ConfigMapping.getMapping();
    assertEquals(value, "registry.client.serviceUrl.defaultZone");
    assertNotNull(m);
  }

  @Test
  public void testConvertedMap() {
    String value = ConfigMapping.map("eureka.client.serviceUrl.defaultZone");
    Map<String, Object> m = ConfigMapping.getMapping();
    Map<String, Object> m1 = ConfigMapping.getConvertedMap(m);
    assertEquals(value, "registry.client.serviceUrl.defaultZone");
    assertNotNull(m1);
  }
}
