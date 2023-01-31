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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;


public final class BeanUtils {
  public static final String DEFAULT_BEAN_CORE_RESOURCE = "classpath*:META-INF/spring/scb-core-bean.xml";

  public static final String DEFAULT_BEAN_NORMAL_RESOURCE = "classpath*:META-INF/spring/*.bean.xml";

  private static ApplicationContext context;

  private BeanUtils() {
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

  public static <T> T getBean(Class<T> type) {
    if (context == null) {
      // for some test case
      return null;
    }
    return context.getBean(type);
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

  public static <T extends SPIOrder & SPIEnabled> void addBeans(Class<T> cls, List<T> exists) {
    if (context == null) {
      return;
    }

    for (T instance : exists) {
      context.getAutowireCapableBeanFactory().autowireBean(instance);
    }
    for (T bean : context.getBeansOfType(cls).values()) {
      if (bean.enabled()) {
        exists.add(bean);
      }
    }
    exists.sort(Comparator.comparingInt(SPIOrder::getOrder));
  }
}
