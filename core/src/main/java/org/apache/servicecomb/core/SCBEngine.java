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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.schema.StaticSchemaFactory;
import org.apache.servicecomb.core.endpoint.AbstractEndpointsCache;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.handler.HandlerConfigUtils;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.task.MicroserviceInstanceRegisterTask;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;

// TODO: should not depend on spring, that will make integration more flexible
public class SCBEngine {
  private static final Logger LOGGER = LoggerFactory.getLogger(SCBEngine.class);

  static final String CFG_KEY_WAIT_UP_TIMEOUT = "servicecomb.boot.waitUp.timeoutInMilliseconds";

  static final long DEFAULT_WAIT_UP_TIMEOUT = 10_000;

  private ProducerProviderManager producerProviderManager;

  private ConsumerProviderManager consumerProviderManager;

  private MicroserviceMeta producerMicroserviceMeta;

  private TransportManager transportManager;

  private SchemaListenerManager schemaListenerManager;

  private Collection<BootListener> bootListenerList;

  private final AtomicLong invocationStartedCounter = new AtomicLong();

  private final AtomicLong invocationFinishedCounter = new AtomicLong();

  private volatile SCBStatus status = SCBStatus.DOWN;

  private EventBus eventBus = EventManager.getEventBus();

  private StaticSchemaFactory staticSchemaFactory;

  private static final SCBEngine INSTANCE = new SCBEngine();

  public void setStatus(SCBStatus status) {
    this.status = status;
  }

  public SCBStatus getStatus() {
    return status;
  }

  public static SCBEngine getInstance() {
    return INSTANCE;
  }

  public EventBus getEventBus() {
    return eventBus;
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
    List<BootListener> tmp = new ArrayList<>();
    tmp.addAll(bootListenerList);
    tmp.addAll(SPIServiceUtils.getOrLoadSortedService(BootListener.class));
    tmp.sort(Comparator.comparingInt(BootListener::getOrder));

    this.bootListenerList = tmp;
  }

  protected void triggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setScbEngine(this);
    event.setEventType(eventType);

    for (BootListener listener : bootListenerList) {
      listener.onBootEvent(event);
    }
  }

  protected void safeTriggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setScbEngine(this);
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
          LOGGER.info("ServiceComb is ready.");
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
        waitStatusUp();
      } catch (TimeoutException e) {
        LOGGER.warn("{}", e.getMessage());
      } catch (Throwable e) {
        destroy();
        status = SCBStatus.FAILED;
        throw new IllegalStateException("ServiceComb init failed.", e);
      }
    }
  }

  private void doInit() throws Exception {
    status = SCBStatus.STARTING;

    eventBus.register(this);

    consumerProviderManager.setAppManager(RegistryUtils.getServiceRegistry().getAppManager());
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

    Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
  }

  /**
   * not allow throw any exception
   * even some step throw exception, must catch it and go on, otherwise shutdown process will be broken.
   */
  public synchronized void destroy() {
    if (SCBStatus.UP.equals(status) || SCBStatus.STARTING.equals(status)) {
      LOGGER.info("ServiceComb is closing now...");
      doDestroy();
      status = SCBStatus.DOWN;
      LOGGER.info("ServiceComb had closed");
    }
  }

  private void doDestroy() {
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

    //Step 6: destroy config center source
    ConfigUtil.destroyConfigCenterConfigurationSource();

    //Step 7: notify all component do clean works via AFTER_CLOSE Event
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

  public void ensureStatusUp() {
    SCBStatus currentStatus = getStatus();
    if (!SCBStatus.UP.equals(currentStatus)) {
      String message =
          "The request is rejected. Cannot process the request due to STATUS = " + currentStatus;
      LOGGER.warn(message);
      throw new InvocationException(Status.SERVICE_UNAVAILABLE, message);
    }
  }

  public ReferenceConfig createReferenceConfigForInvoke(String microserviceName, String versionRule, String transport) {
    ensureStatusUp();

    return consumerProviderManager.createReferenceConfig(microserviceName, versionRule, transport);
  }

  public ReferenceConfig getReferenceConfigForInvoke(String microserviceName) {
    ensureStatusUp();

    return consumerProviderManager.getReferenceConfig(microserviceName);
  }

  public MicroserviceMeta getProducerMicroserviceMeta() {
    return producerMicroserviceMeta;
  }

  public void setProducerMicroserviceMeta(MicroserviceMeta producerMicroserviceMeta) {
    this.producerMicroserviceMeta = producerMicroserviceMeta;
  }

  /**
   * better to subscribe EventType.AFTER_REGISTRY by BootListener<br>
   * but in some simple scenes, just block and wait is enough.
   */
  public void waitStatusUp() throws InterruptedException, TimeoutException {
    long msWait = DynamicPropertyFactory.getInstance().getLongProperty(CFG_KEY_WAIT_UP_TIMEOUT, DEFAULT_WAIT_UP_TIMEOUT)
        .get();
    waitStatusUp(msWait);
  }

  /**
   * better to subscribe EventType.AFTER_REGISTRY by BootListener<br>
   * but in some simple scenes, just block and wait is enough.
   */
  public void waitStatusUp(long msWait) throws InterruptedException, TimeoutException {
    if (msWait <= 0) {
      LOGGER.info("Give up waiting for status up, wait timeout milliseconds={}.", msWait);
      return;
    }

    LOGGER.info("Waiting for status up. timeout: {}ms", msWait);
    long start = System.currentTimeMillis();
    for (; ; ) {
      SCBStatus currentStatus = getStatus();
      switch (currentStatus) {
        case DOWN:
        case FAILED:
          throw new IllegalStateException("Failed to wait status up, real status: " + currentStatus);
        case UP:
          LOGGER.info("Status already changed to up.");
          return;
        default:
          break;
      }

      TimeUnit.MILLISECONDS.sleep(100);
      if (System.currentTimeMillis() - start > msWait) {
        throw new TimeoutException(
            String.format("Timeout to wait status up, timeout: %dms, last status: %s", msWait, currentStatus));
      }
    }
  }

  public StaticSchemaFactory getStaticSchemaFactory() {
    return staticSchemaFactory;
  }

  public void setStaticSchemaFactory(StaticSchemaFactory staticSchemaFactory) {
    this.staticSchemaFactory = staticSchemaFactory;
  }
}
