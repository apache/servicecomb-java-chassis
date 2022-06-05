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
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;
import org.springframework.core.Ordered;

import mockit.Deencapsulation;
import mockit.Expectations;

/**
 * Test SPIServiceUtils
 *
 *
 */
public class TestSPIServiceUtils {
  @Test
  public void testGetTargetServiceNull() {
    SPIServiceDef0 service = SPIServiceUtils.getTargetService(SPIServiceDef0.class);
    Assertions.assertNull(service);
  }

  @Test
  public void testGetTargetServiceNotNull() {
    SPIServiceDef service = SPIServiceUtils.getTargetService(SPIServiceDef.class);
    Assertions.assertNotNull(service);

    Assertions.assertSame(service, SPIServiceUtils.getTargetService(SPIServiceDef.class));
  }

  @Test
  public void testGetTargetService_special_null() {
    Assertions.assertNull(SPIServiceUtils.getTargetService(SPIServiceDef0.class, SPIServiceDef0Impl.class));
  }

  @Test
  public void testGetTargetService_special_notNull() {
    SPIServiceDef service = SPIServiceUtils.getTargetService(SPIServiceDef.class, SPIServiceDefImpl.class);
    Assertions.assertNotNull(service);
  }

  @Test
  @EnabledOnJre(JRE.JAVA_8)
  public void testSort() {
    Ordered o1 = Mockito.mock(Ordered.class);
    Ordered o2 = Mockito.mock(Ordered.class);
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

    MatcherAssert.assertThat(SPIServiceUtils.getSortedService(Ordered.class), Matchers.contains(o1, o2));
    MatcherAssert.assertThat(SPIServiceUtils.getAllService(Ordered.class), Matchers.contains(o1, o2));
    MatcherAssert.assertThat(SPIServiceUtils.getPriorityHighestService(Ordered.class), Matchers.is(o1));

    Map<Class<?>, List<Object>> cache = Deencapsulation.getField(SPIServiceUtils.class, "cache");
    cache.clear();
  }

  @Test
  public void getPriorityHighestService_null() {
    Assertions.assertNull(SPIServiceUtils.getPriorityHighestService(SPIServiceDef0.class));
  }

  interface PriorityIntf {
    String getName();

    int getOrder();
  }

  public static class PriorityImpl implements PriorityIntf {
    private final String name;

    private final int order;

    public PriorityImpl(String name, int order) {
      this.name = name;
      this.order = order;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getOrder() {
      return order;
    }

    @Override
    public String toString() {
      return "PriorityImpl{" +
              "name='" + name + '\'' +
              ", order=" + order +
              '}';
    }
  }

  @Test
  @EnabledOnJre(JRE.JAVA_8)
  public void getPriorityHighestServices() {
    Map<String, PriorityIntf> instances = new LinkedHashMap<>();
    instances.putIfAbsent("1", new PriorityImpl("n1", 0));
    instances.putIfAbsent("2", new PriorityImpl("n1", -1));
    instances.putIfAbsent("3", new PriorityImpl("n1", 1));
    instances.putIfAbsent("4", new PriorityImpl("n2", 0));
    instances.putIfAbsent("5", new PriorityImpl("n2", -1));
    instances.putIfAbsent("6", new PriorityImpl("n2", 1));

    ServiceLoader<PriorityIntf> serviceLoader = ServiceLoader.load(PriorityIntf.class);
    Deencapsulation.setField(serviceLoader, "providers", instances);
    new Expectations(ServiceLoader.class) {
      {
        ServiceLoader.load(PriorityIntf.class);
        result = serviceLoader;
      }
    };

    MatcherAssert.assertThat(SPIServiceUtils.getPriorityHighestServices(PriorityIntf::getName, PriorityIntf.class),
            Matchers.containsInAnyOrder(instances.get("2"), instances.get("5")));

    Map<Class<?>, List<Object>> cache = Deencapsulation.getField(SPIServiceUtils.class, "cache");
    cache.clear();
  }
}
