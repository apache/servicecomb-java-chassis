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
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;

public class SCBApplicationListener
    implements ApplicationListener<ApplicationEvent>, Ordered, ApplicationContextAware {
  private Class<?> initEventClass = ContextRefreshedEvent.class;

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if (this.applicationContext == applicationContext) {
      // same object. avoid initialize many times.
      return;
    }
    this.applicationContext = applicationContext;
    BeanUtils.setContext(applicationContext);
    HttpClients.load();
    RegistrationManager.INSTANCE.init();
    DiscoveryManager.INSTANCE.init();
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

      SCBEngine scbEngine = SCBEngine.getInstance();
      //SCBEngine init first, hence we do not need worry that when other beans need use the
      //producer microserviceMeta, the SCBEngine is not inited.
//        String serviceName = RegistryUtils.getMicroservice().getServiceName();
//        SCBEngine.getInstance().setProducerMicroserviceMeta(new MicroserviceMeta(serviceName).setConsumer(false));
//        SCBEngine.getInstance().setProducerProviderManager(applicationContext.getBean(ProducerProviderManager.class));
//        SCBEngine.getInstance().setConsumerProviderManager(applicationContext.getBean(ConsumerProviderManager.class));
//        SCBEngine.getInstance().setTransportManager(applicationContext.getBean(TransportManager.class));
      scbEngine.setApplicationContext(applicationContext);
      scbEngine.setPriorityPropertyManager(applicationContext.getBean(PriorityPropertyManager.class));
      scbEngine.setFilterChainsManager(applicationContext.getBean(FilterChainsManager.class));
      scbEngine.getConsumerProviderManager().getConsumerProviderList()
          .addAll(applicationContext.getBeansOfType(ConsumerProvider.class).values());
      scbEngine.getProducerProviderManager().getProducerProviderList()
          .addAll(applicationContext.getBeansOfType(ProducerProvider.class).values());
      scbEngine.addBootListeners(applicationContext.getBeansOfType(BootListener.class).values());

      scbEngine.run();
    } else if (event instanceof ContextClosedEvent) {
      if (SCBEngine.getInstance() != null) {
        SCBEngine.getInstance().destroy();
      }
    }
  }
}
