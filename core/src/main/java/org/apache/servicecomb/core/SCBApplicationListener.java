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

package org.apache.servicecomb.core;

import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.core.filter.FilterChainsManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

public class SCBApplicationListener
    implements ApplicationListener<ApplicationEvent>, Ordered, ApplicationContextAware, EnvironmentAware {
  private Class<?> initEventClass = ContextRefreshedEvent.class;

  private ApplicationContext applicationContext;

  private final SCBEngine scbEngine;

  public SCBApplicationListener(SCBEngine scbEngine) {
    this.scbEngine = scbEngine;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if (this.applicationContext == applicationContext) {
      // same object. avoid initialize many times.
      return;
    }
    this.applicationContext = applicationContext;
    BeanUtils.setContext(applicationContext);
    HttpClients.load();
  }

  @Override
  public void setEnvironment(Environment environment) {
    scbEngine.init();
  }

  public void setInitEventClass(Class<?> initEventClass) {
    this.initEventClass = initEventClass;
  }

  @Override
  public int getOrder() {
    // should run before default listener, eg: ZuulConfiguration
    return -1000;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (initEventClass.isInstance(event)) {
      if (applicationContext instanceof AbstractApplicationContext) {
        ((AbstractApplicationContext) applicationContext).registerShutdownHook();
      }

      scbEngine.setPriorityPropertyManager(applicationContext.getBean(PriorityPropertyManager.class));
      scbEngine.setFilterChainsManager(applicationContext.getBean(FilterChainsManager.class));
      scbEngine.getConsumerProviderManager().getConsumerProviderList()
          .addAll(applicationContext.getBeansOfType(ConsumerProvider.class).values());
      scbEngine.getProducerProviderManager().getProducerProviderList()
          .addAll(applicationContext.getBeansOfType(ProducerProvider.class).values());

      scbEngine.run();
    } else if (event instanceof ContextClosedEvent) {
      if (SCBEngine.getInstance() != null) {
        SCBEngine.getInstance().destroy();
      }
    }
  }
}
