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

package org.apache.servicecomb.config.inject;

import java.util.Collections;

import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class InjectBeanPostProcessor implements BeanPostProcessor {
  private final PriorityPropertyManager priorityPropertyManager;

  public InjectBeanPostProcessor(PriorityPropertyManager priorityPropertyManager) {
    this.priorityPropertyManager = priorityPropertyManager;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> beanCls = BeanUtils.getImplClassFromBean(bean);
    InjectProperties injectProperties = beanCls.getAnnotation(InjectProperties.class);
    if (injectProperties == null) {
      return bean;
    }

    priorityPropertyManager.createConfigObject(bean, Collections.emptyMap());

    return bean;
  }
}
