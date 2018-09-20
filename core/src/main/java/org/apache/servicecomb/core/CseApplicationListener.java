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

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.schema.StaticSchemaFactory;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;

public class CseApplicationListener
    implements ApplicationListener<ApplicationEvent>, Ordered, ApplicationContextAware {
  private Class<?> initEventClass = ContextRefreshedEvent.class;

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    BeanUtils.setContext(applicationContext);
    RegistryUtils.init();
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

      if (SCBEngine.getInstance().getBootListenerList() == null) {
        //SCBEngine init first, hence we do not need worry that when other beans need use the
        //producer microserviceMeta, the SCBEngine is not inited.
        String serviceName = RegistryUtils.getMicroservice().getServiceName();
        SCBEngine.getInstance().setProducerMicroserviceMeta(new MicroserviceMeta(serviceName));
        SCBEngine.getInstance().setProducerProviderManager(applicationContext.getBean(ProducerProviderManager.class));
        SCBEngine.getInstance().setConsumerProviderManager(applicationContext.getBean(ConsumerProviderManager.class));
        SCBEngine.getInstance().setTransportManager(applicationContext.getBean(TransportManager.class));
        SCBEngine.getInstance().setSchemaListenerManager(applicationContext.getBean(SchemaListenerManager.class));
        SCBEngine.getInstance().setBootListenerList(applicationContext.getBeansOfType(BootListener.class).values());
        SCBEngine.getInstance().setStaticSchemaFactory(applicationContext.getBean(StaticSchemaFactory.class));
      }

      SCBEngine.getInstance().init();
    } else if (event instanceof ContextClosedEvent) {
      SCBEngine.getInstance().destroy();
    }
  }
}
