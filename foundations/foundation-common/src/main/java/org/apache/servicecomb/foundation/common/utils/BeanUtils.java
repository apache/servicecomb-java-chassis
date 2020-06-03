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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public final class BeanUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);

  public static final String DEFAULT_BEAN_CORE_RESOURCE = "classpath*:META-INF/spring/scb-core-bean.xml";

  public static final String DEFAULT_BEAN_NORMAL_RESOURCE = "classpath*:META-INF/spring/*.bean.xml";

  public static final String[] DEFAULT_BEAN_RESOURCE = new String[] {DEFAULT_BEAN_CORE_RESOURCE
      , DEFAULT_BEAN_NORMAL_RESOURCE};

  public static final String SCB_SCAN_PACKAGE = "scb-scan-package";

  private static final String SCB_PACKAGE = "org.apache.servicecomb";

  private static ApplicationContext context;

  private BeanUtils() {
  }

  public static void init() {
    init(DEFAULT_BEAN_RESOURCE);
  }

  public static void init(String... configLocations) {
    prepareServiceCombScanPackage();

    Set<String> locationSet = new LinkedHashSet<>();
    addBeanLocation(locationSet, DEFAULT_BEAN_RESOURCE);
    addBeanLocation(locationSet, configLocations);
    context = new ClassPathXmlApplicationContext(locationSet.toArray(new String[locationSet.size()]));
  }

  public static void addBeanLocation(Set<String> locationSet, String... location) {
    Arrays.stream(location).forEach(loc -> addBeanLocation(locationSet, loc));
  }

  public static void addBeanLocation(Set<String> locationSet, String location) {
    if (location == null) {
      return;
    }

    location = location.trim();
    if (StringUtils.isNotEmpty(location)) {
      locationSet.add(location);
    }
  }

  private static void addItem(Set<String> set, String item) {
    for (String it : set) {
      if (item.startsWith(it)) {
        return;
      }
    }
    set.add(item);
  }

  public static void prepareServiceCombScanPackage() {
    Set<String> scanPackags = new LinkedHashSet<>();
    // add exists settings
    String exists = System.getProperty(SCB_SCAN_PACKAGE);
    if (exists != null) {
      for (String exist : exists.trim().split(",")) {
        if (!exist.isEmpty()) {
          addItem(scanPackags, exist.trim());
        }
      }
    }

    // ensure servicecomb package exist
    addItem(scanPackags, SCB_PACKAGE);

    // add main class package
    for (Class<?> mainClass : new Class<?>[] {JvmUtils.findMainClass(), JvmUtils.findMainClassByStackTrace()}) {
      if (mainClass != null) {
        String pkg = mainClass.getPackage().getName();
        addItem(scanPackags, pkg);
      }
    }

    // finish
    String scbScanPackages = StringUtils.join(scanPackags, ",");
    System.setProperty(SCB_SCAN_PACKAGE, scbScanPackages);
    LOGGER.info("Scb scan package list: " + scbScanPackages);
  }

  public static ApplicationContext getContext() {
    return context;
  }

  public static void setContext(ApplicationContext applicationContext) {
    context = applicationContext;
  }

  /**
   * 不应该在业务流程中频繁调用，因为内部必然会加一个锁做互斥，会影响并发度
   */
  @SuppressWarnings("unchecked")
  public static <T> T getBean(String name) {
    return (T) context.getBean(name);
  }

  public static <T> Map<String, T> getBeansOfType(Class<T> type) {
    if (context == null) {
      // for some test case
      return Collections.emptyMap();
    }
    return context.getBeansOfType(type);
  }

  /**
   * Get the implemented class of the given instance
   * @param bean the instance to get implemented class from
   * @return the implemented class (if the checked class is proxied, return the ultimate target class)
   * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass
   */
  public static Class<?> getImplClassFromBean(Object bean) {
    return AopProxyUtils.ultimateTargetClass(bean);
  }
}
