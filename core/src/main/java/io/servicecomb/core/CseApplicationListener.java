/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core;

import java.util.Collection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import com.google.common.eventbus.Subscribe;

import io.servicecomb.core.BootListener.BootEvent;
import io.servicecomb.core.BootListener.EventType;
import io.servicecomb.core.definition.loader.SchemaListenerManager;
import io.servicecomb.core.endpoint.AbstractEndpointsCache;
import io.servicecomb.core.handler.HandlerConfigUtils;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import io.servicecomb.core.provider.producer.ProducerProviderManager;
import io.servicecomb.core.transport.TransportManager;
import io.servicecomb.foundation.common.event.EventManager;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.FortifyUtils;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;

public class CseApplicationListener
    implements ApplicationListener<ApplicationEvent>, Ordered, ApplicationContextAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(CseApplicationListener.class);

  private static boolean isInit = false;

  @Inject
  private ProducerProviderManager producerProviderManager;

  @Inject
  private ConsumerProviderManager consumerProviderManager;

  @Inject
  private TransportManager transportManager;

  @Inject
  private SchemaListenerManager schemaListenerManager;

  private Collection<BootListener> bootListenerList;

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

  protected void triggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setEventType(eventType);

    for (BootListener listener : bootListenerList) {
      listener.onBootEvent(event);
    }
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (initEventClass.isInstance(event)) {
      //TODO to load when webapplication context is used for discovery client, need to check if can use the order and undo this change with proper fix.
      if (!isInit) {
        try {
          bootListenerList = applicationContext.getBeansOfType(BootListener.class).values();

          AbstractEndpointsCache.init(RegistryUtils.getInstanceCacheManager(), transportManager);

          triggerEvent(EventType.BEFORE_HANDLER);
          HandlerConfigUtils.init();
          triggerEvent(EventType.AFTER_HANDLER);

          triggerEvent(EventType.BEFORE_PRODUCER_PROVIDER);
          producerProviderManager.init();
          triggerEvent(EventType.AFTER_PRODUCER_PROVIDER);

          triggerEvent(EventType.BEFORE_CONSUMER_PROVIDER);
          consumerProviderManager.init();
          triggerEvent(EventType.AFTER_CONSUMER_PROVIDER);

          triggerEvent(EventType.BEFORE_TRANSPORT);
          transportManager.init();
          triggerEvent(EventType.AFTER_TRANSPORT);

          schemaListenerManager.notifySchemaListener();

          triggerEvent(EventType.BEFORE_REGISTRY);
          /*
           * 由于注册实例是异步过程，所以后续的AFTER_REGISTRY消息不能立即发送，必须等到实例第一次注册成功时才能发送。
           * 将监听器的注册逻辑放在RegistryUtils.run()之前，以防止MicroserviceInstanceRegisterTask消息在监听器注册之前就被post而漏过。
           * 实例注册操作完成时，会在EventManager中post一次MicroserviceInstanceRegisterTask，监听此消息以触发后续操作。
           * 判断实例是否注册成功的标准是InstanceId是否为空。
           * 操作完成后将此监听器从EventManager中去除。
           */
          EventManager.register(new Object() {
            @Subscribe
            public void afterRegistryInstance(MicroserviceInstanceRegisterTask microserviceInstanceRegisterTask) {
              LOGGER.info("receive MicroserviceInstanceRegisterTask event, check instance Id...");
              if (!StringUtils.isEmpty(RegistryUtils.getMicroserviceInstance().getInstanceId())) {
                LOGGER.info("instance registry succeeds for the first time, will send AFTER_REGISTRY event.");
                ReferenceConfigUtils.setReady(true);
                triggerEvent(EventType.AFTER_REGISTRY);
                EventManager.unregister(this);
              }
            }
          });
          RegistryUtils.run();

          // 当程序退出时，进行相关清理，注意：kill -9 {pid}下无效
          // 1. 去注册实例信息
          // TODO 服务优雅退出
          if (applicationContext instanceof AbstractApplicationContext) {
            ((AbstractApplicationContext) applicationContext).registerShutdownHook();
          }
          isInit = true;
        } catch (Exception e) {
          LOGGER.error("cse init failed, {}", FortifyUtils.getErrorInfo(e));
        }
      }
    } else if (event instanceof ContextClosedEvent) {
      LOGGER.warn("cse is closing now...");
      RegistryUtils.destory();
      isInit = false;
    }
  }
}
