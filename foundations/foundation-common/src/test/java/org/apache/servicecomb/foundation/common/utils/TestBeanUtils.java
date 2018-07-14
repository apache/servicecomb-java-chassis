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

import org.aspectj.lang.annotation.Aspect;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import mockit.Expectations;
import mockit.Mocked;

public class TestBeanUtils {
  static interface Intf {

  }

  static class Impl implements Intf {

  }

  @Aspect
  static class MyAspect {
  }

  @BeforeClass
  public static void setup() {
    System.clearProperty(BeanUtils.SCB_SCAN_PACKAGE);
  }

  @AfterClass
  public static void tearDown() {
    System.clearProperty(BeanUtils.SCB_SCAN_PACKAGE);
  }

  @Test
  public void test() {
    Intf target = new Impl();
    AspectJProxyFactory factory = new AspectJProxyFactory(target);
    MyAspect aspect = new MyAspect();
    factory.addAspect(aspect);
    Intf proxy = factory.getProxy();

    Assert.assertEquals(Impl.class, BeanUtils.getImplClassFromBean(proxy));
    Assert.assertEquals(Impl.class, BeanUtils.getImplClassFromBean(new Impl()));
  }

  @Test
  public void prepareServiceCombScanPackage_noExist_noMain() {
    System.clearProperty(BeanUtils.SCB_SCAN_PACKAGE);
    new Expectations(JvmUtils.class) {
      {
        JvmUtils.findMainClass();
        result = null;
      }
    };

    BeanUtils.prepareServiceCombScanPackage();

    Assert.assertEquals("org.apache.servicecomb", System.getProperty(BeanUtils.SCB_SCAN_PACKAGE));
  }

  @Test
  public void prepareServiceCombScanPackage_noExist_scbMain() {
    System.clearProperty(BeanUtils.SCB_SCAN_PACKAGE);
    new Expectations(JvmUtils.class) {
      {
        JvmUtils.findMainClass();
        result = TestBeanUtils.class;
      }
    };

    BeanUtils.prepareServiceCombScanPackage();

    Assert.assertEquals("org.apache.servicecomb", System.getProperty(BeanUtils.SCB_SCAN_PACKAGE));
  }

  @Test
  public void prepareServiceCombScanPackage_noExist_otherMain() {
    System.clearProperty(BeanUtils.SCB_SCAN_PACKAGE);
    new Expectations(JvmUtils.class) {
      {
        JvmUtils.findMainClass();
        result = String.class;
      }
    };

    BeanUtils.prepareServiceCombScanPackage();

    Assert.assertEquals("org.apache.servicecomb,java.lang", System.getProperty(BeanUtils.SCB_SCAN_PACKAGE));
  }

  @Test
  public void prepareServiceCombScanPackage_exist() {
    System.setProperty(BeanUtils.SCB_SCAN_PACKAGE, "a.b,,c.d");
    new Expectations(JvmUtils.class) {
      {
        JvmUtils.findMainClass();
        result = null;
      }
    };

    BeanUtils.prepareServiceCombScanPackage();

    Assert.assertEquals("a.b,c.d,org.apache.servicecomb", System.getProperty(BeanUtils.SCB_SCAN_PACKAGE));
  }

  @Test
  public void init(@Mocked ClassPathXmlApplicationContext context) {
    System.clearProperty(BeanUtils.SCB_SCAN_PACKAGE);
    new Expectations(JvmUtils.class) {
      {
        JvmUtils.findMainClass();
        result = TestBeanUtils.class;
      }
    };
    BeanUtils.init();

    Assert.assertEquals("org.apache.servicecomb", System.getProperty(BeanUtils.SCB_SCAN_PACKAGE));
  }
}
