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

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.endpoint.AbstractEndpointsCache;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.handler.HandlerConfigUtils;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

// TODO: should not depend on spring, that will make integration more flexible
public class SCBEngine {
  private static final Logger LOGGER = LoggerFactory.getLogger(SCBEngine.class);

  private ProducerProviderManager producerProviderManager;

  private ConsumerProviderManager consumerProviderManager;

  private TransportManager transportManager;

  private SchemaListenerManager schemaListenerManager;

  private Collection<BootListener> bootListenerList;

  private final AtomicLong invocationStartedCounter = new AtomicLong();

  private final AtomicLong invocationFinishedCounter = new AtomicLong();

  private volatile SCBStatus status = SCBStatus.DOWN;

  public void setStatus(SCBStatus status) {
    this.status = status;
  }

  public SCBStatus getStatus() {
    return status;
  }

  private EventBus eventBus = EventManager.getEventBus();

  private static final SCBEngine INSTANCE = new SCBEngine();

  public static SCBEngine getInstance() {
    return INSTANCE;
  }

  public void setProducerProviderManager(
      ProducerProviderManager producerProviderManager) {
    this.producerProviderManager = producerProviderManager;
  }

  public void setConsumerProviderManager(
      ConsumerProviderManager consumerProviderManager) {
    this.consumerProviderManager = consumerProviderManager;
  }

  public TransportManager getTransportManager() {
    return transportManager;
  }

  public void setTransportManager(TransportManager transportManager) {
    this.transportManager = transportManager;
  }

  public void setSchemaListenerManager(
      SchemaListenerManager schemaListenerManager) {
    this.schemaListenerManager = schemaListenerManager;
  }

  public Collection<BootListener> getBootListenerList() {
    return bootListenerList;
  }

  public void setBootListenerList(Collection<BootListener> bootListenerList) {
    this.bootListenerList = bootListenerList;
  }

  protected void triggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setEventType(eventType);

    for (BootListener listener : bootListenerList) {
      listener.onBootEvent(event);
    }
  }

  protected void safeTriggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setEventType(eventType);

    for (BootListener listener : bootListenerList) {
      try {
        listener.onBootEvent(event);
        LOGGER.info("BootListener {} succeed to process {}.", listener.getClass().getName(), eventType);
      } catch (Throwable e) {
        LOGGER.error("BootListener {} failed to process {}.", listener.getClass().getName(), eventType, e);
      }
    }
  }

  /**
   * <p>As the process of instance registry is asynchronous, the {@code AFTER_REGISTRY}
   * event should not be sent immediately after {@link RegistryUtils#run()} is invoked.
   * When the instance registry succeeds, {@link MicroserviceInstanceRegisterTask} will be posted in {@link EventManager},
   * register a subscriber to watch this event and send {@code AFTER_REGISTRY}.</p>
   *
   * <p>This method should be called before {@link RegistryUtils#run()} to avoid that the registry process is too quick
   * that the event is not watched by this subscriber.</p>
   *
   * <p>Check if {@code InstanceId} is null to judge whether the instance registry has succeeded.</p>
   */
  private void triggerAfterRegistryEvent() {
    EventManager.register(new Object() {
      @Subscribe
      public void afterRegistryInstance(MicroserviceInstanceRegisterTask microserviceInstanceRegisterTask) {
        LOGGER.info("receive MicroserviceInstanceRegisterTask event, check instance Id...");
        if (!StringUtils.isEmpty(RegistryUtils.getMicroserviceInstance().getInstanceId())) {
          LOGGER.info("instance registry succeeds for the first time, will send AFTER_REGISTRY event.");
          status = SCBStatus.UP;
          triggerEvent(EventType.AFTER_REGISTRY);
          EventManager.unregister(this);
        }
      }
    });
  }

  @AllowConcurrentEvents
  @Subscribe
  public void onInvocationStart(InvocationStartEvent event) {
    invocationStartedCounter.incrementAndGet();
  }

  @AllowConcurrentEvents
  @Subscribe
  public void onInvocationFinish(InvocationFinishEvent event) {
    invocationFinishedCounter.incrementAndGet();
  }

  public synchronized void init() {
    if (SCBStatus.DOWN.equals(status)) {
      try {
        doInit();
        status = SCBStatus.UP;
      } catch (Exception e) {
        uninit();
        status = SCBStatus.FAILED;
        throw new IllegalStateException("ServiceComb init failed.", e);
      }
    }
  }


  private void doInit() throws Exception {
    status = SCBStatus.STARTING;

    eventBus.register(this);

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

    triggerAfterRegistryEvent();

    RegistryUtils.run();

    Runtime.getRuntime().addShutdownHook(new Thread(this::uninit));
  }

  /**
   * not allow throw any exception
   * even some step throw exception, must catch it and go on, otherwise shutdown process will be broken.
   */
  public synchronized void uninit() {
    if (SCBStatus.UP.equals(status)) {
      LOGGER.info("ServiceComb is closing now...");
      doUninit();
      status = SCBStatus.DOWN;
      LOGGER.info("ServiceComb had closed");
    }
  }

  private void doUninit() {
    //Step 1: notify all component stop invoke via BEFORE_CLOSE Event
    safeTriggerEvent(EventType.BEFORE_CLOSE);

    //Step 2: forbid create new consumer invocation
    status = SCBStatus.STOPPING;

    //Step 3: Unregister microservice instance from Service Center and close vertx
    // Forbidden other consumers find me
    RegistryUtils.destroy();
    VertxUtils.blockCloseVertxByName("registry");

    //Step 4: wait all invocation finished
    try {
      validAllInvocationFinished();
    } catch (InterruptedException e) {
      LOGGER.error("wait all invocation finished interrupted", e);
    }

    //Step 5: Stop vertx to prevent blocking exit
    VertxUtils.blockCloseVertxByName("config-center");
    VertxUtils.blockCloseVertxByName("transport");

    //Step 6: notify all component do clean works via AFTER_CLOSE Event
    safeTriggerEvent(EventType.AFTER_CLOSE);
  }

  private void validAllInvocationFinished() throws InterruptedException {
    while (true) {
      if (invocationFinishedCounter.get() == invocationStartedCounter.get()) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
  }
}
