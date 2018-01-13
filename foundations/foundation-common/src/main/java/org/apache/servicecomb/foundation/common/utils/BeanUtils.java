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

import org.springframework.aop.TargetClassAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class BeanUtils {
  public static final String DEFAULT_BEAN_RESOURCE = "classpath*:META-INF/spring/*.bean.xml";

  private static ApplicationContext context;

  private BeanUtils() {
  }

  public static void init() {
    init(DEFAULT_BEAN_RESOURCE);
  }

  public static void init(String... configLocations) {
    context = new ClassPathXmlApplicationContext(configLocations);
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

  public static Class<?> getImplClassFromBean(Object bean) {
    if (TargetClassAware.class.isInstance(bean)) {
      return ((TargetClassAware) bean).getTargetClass();
    }

    return bean.getClass();
  }
}
