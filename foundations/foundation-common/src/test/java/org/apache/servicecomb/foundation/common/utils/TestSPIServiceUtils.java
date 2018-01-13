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

package org.apache.servicecomb.foundation.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.Ordered;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

/**
 * Test SPIServiceUtils 
 *
 *
 */
public class TestSPIServiceUtils {
  @Test
  public void testGetTargetServiceNull() {
    SPIServiceDef0 service = SPIServiceUtils.getTargetService(SPIServiceDef0.class);
    Assert.assertNull(service);
  }

  @Test
  public void testGetTargetServiceNotNull() {
    SPIServiceDef service = SPIServiceUtils.getTargetService(SPIServiceDef.class);
    Assert.assertTrue(SPIServiceDef.class.isInstance(service));
  }

  @Test
  public void testSort(@Mocked Ordered o1, @Mocked Ordered o2) {
    Map<String, Ordered> map = new LinkedHashMap<>();
    map.put("a", o1);
    map.put("b", o2);

    ServiceLoader<Ordered> serviceLoader = ServiceLoader.load(Ordered.class);
    Deencapsulation.setField(serviceLoader, "providers", map);
    new Expectations(ServiceLoader.class) {
      {
        o1.getOrder();
        result = -1;
        o2.getOrder();
        result = Integer.MAX_VALUE;
        ServiceLoader.load(Ordered.class);
        result = serviceLoader;
      }
    };

    Assert.assertThat(SPIServiceUtils.getSortedService(Ordered.class), Matchers.contains(o1, o2));
    Assert.assertThat(SPIServiceUtils.getPriorityHighestService(Ordered.class), Matchers.is(o1));
  }
}
