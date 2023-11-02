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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.bootup.BootUpInformationCollector;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceVersionsMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.filter.FilterChainsManager;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfigManager;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jakarta.ws.rs.core.Response.Status;

public class SCBEngine {
  private static final Logger LOGGER = LoggerFactory.getLogger(SCBEngine.class);

  public static final String CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC = "servicecomb.boot.turnDown.waitInSeconds";

  public static final long DEFAULT_TURN_DOWN_STATUS_WAIT_SEC = 0;

  // TODO: will remove in future. Too many codes need refactor.
  private static volatile SCBEngine INSTANCE;

  private ApplicationContext applicationContext;

  private FilterChainsManager filterChainsManager;

  private ProducerProviderManager producerProviderManager;

  private ConsumerProviderManager consumerProviderManager = new ConsumerProviderManager();

  private MicroserviceMeta producerMicroserviceMeta;

  private TransportManager transportManager;

  private List<BootListener> bootListeners;

  private final AtomicLong invocationStartedCounter = new AtomicLong();

  private final AtomicLong invocationFinishedCounter = new AtomicLong();

  private volatile SCBStatus status = SCBStatus.DOWN;

  private final EventBus eventBus;

  private ExecutorManager executorManager;

  private PriorityPropertyManager priorityPropertyManager;

  private List<BootUpInformationCollector> bootUpInformationCollectors;

  private final SwaggerEnvironment swaggerEnvironment = new SwaggerEnvironment();

  private OpenAPIRegistryManager openAPIRegistryManager;

  private RegistrationManager registrationManager;

  private DiscoveryManager discoveryManager;

  private Environment environment;

  private ReferenceConfigManager referenceConfigManager;

  public SCBEngine() {
    eventBus = EventManager.getEventBus();

    eventBus.register(this);

    INSTANCE = this;

    producerProviderManager = new ProducerProviderManager(this);
  }

  public static SCBEngine getInstance() {
    if (INSTANCE == null) {
      throw new InvocationException(Status.SERVICE_UNAVAILABLE,
          new CommonExceptionData("SCBEngine is not initialized yet."));
    }
    return INSTANCE;
  }

  @Autowired
  public void setReferenceConfigManager(ReferenceConfigManager referenceConfigManager) {
    this.referenceConfigManager = referenceConfigManager;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public Environment getEnvironment() {
    return this.environment;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setBootUpInformationCollectors(List<BootUpInformationCollector> bootUpInformationCollectors) {
    this.bootUpInformationCollectors = bootUpInformationCollectors;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setBootListeners(List<BootListener> listeners) {
    this.bootListeners = listeners;
  }

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Autowired
  public void setRegistrationManager(RegistrationManager registrationManager) {
    this.registrationManager = registrationManager;
  }

  @Autowired
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Autowired
  public void setOpenAPIRegistryManager(OpenAPIRegistryManager openAPIRegistryManager) {
    this.openAPIRegistryManager = openAPIRegistryManager;
  }

  @Autowired
  public void setExecutorManager(ExecutorManager executorManager) {
    this.executorManager = executorManager;
  }

  @Autowired
  public void setTransportManager(TransportManager transportManager) {
    this.transportManager = transportManager;
  }

  public RegistrationManager getRegistrationManager() {
    return this.registrationManager;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public String getAppId() {
    return BootStrapProperties.readApplication(environment);
  }

  public void setStatus(SCBStatus status) {
    this.status = status;
  }

  public SCBStatus getStatus() {
    return status;
  }

  public OpenAPIRegistryManager getOpenAPIRegistryManager() {
    return this.openAPIRegistryManager;
  }

  public FilterChainsManager getFilterChainsManager() {
    return filterChainsManager;
  }

  public SCBEngine setFilterChainsManager(FilterChainsManager filterChainsManager) {
    this.filterChainsManager = filterChainsManager;
    return this;
  }

  public PriorityPropertyManager getPriorityPropertyManager() {
    return priorityPropertyManager;
  }

  public SCBEngine setPriorityPropertyManager(PriorityPropertyManager priorityPropertyManager) {
    this.priorityPropertyManager = priorityPropertyManager;
    return this;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  public ExecutorManager getExecutorManager() {
    return executorManager;
  }

  public ProducerProviderManager getProducerProviderManager() {
    return producerProviderManager;
  }

  public void setProducerProviderManager(ProducerProviderManager producerProviderManager) {
    this.producerProviderManager = producerProviderManager;
  }

  public ConsumerProviderManager getConsumerProviderManager() {
    return consumerProviderManager;
  }

  public SCBEngine setConsumerProviderManager(ConsumerProviderManager consumerProviderManager) {
    this.consumerProviderManager = consumerProviderManager;
    return this;
  }

  public TransportManager getTransportManager() {
    return transportManager;
  }

  public SwaggerEnvironment getSwaggerEnvironment() {
    return swaggerEnvironment;
  }

  public SCBEngine addProducerMeta(String schemaId, Object instance) {
    getProducerProviderManager().addProducerMeta(schemaId, instance);
    return this;
  }

  protected void triggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setScbEngine(this);
    event.setEventType(eventType);

    for (BootListener listener : bootListeners) {
      listener.onBootEvent(event);
    }
  }

  protected void safeTriggerEvent(EventType eventType) {
    BootEvent event = new BootEvent();
    event.setScbEngine(this);
    event.setEventType(eventType);

    for (BootListener listener : bootListeners) {
      try {
        listener.onBootEvent(event);
        LOGGER.info("BootListener {} succeed to process {}.", listener.getClass().getName(), eventType);
      } catch (Throwable e) {
        LOGGER.error("BootListener {} failed to process {}.", listener.getClass().getName(), eventType, e);
      }
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  @SuppressWarnings("unused")
  public void onInvocationStart(InvocationStartEvent event) {
    invocationStartedCounter.incrementAndGet();
  }

  @AllowConcurrentEvents
  @Subscribe
  @SuppressWarnings("unused")
  public void onInvocationFinish(InvocationFinishEvent event) {
    invocationFinishedCounter.incrementAndGet();
  }

  public synchronized SCBEngine init() {
    this.discoveryManager.init();
    this.registrationManager.init();
    return this;
  }

  public synchronized SCBEngine run() {
    if (SCBStatus.DOWN.equals(status)) {
      try {
        doRun();
        printServiceInfo();
      } catch (Throwable e) {
        LOGGER.error("Failed to start ServiceComb due to errors and close", e);
        try {
          destroy();
        } catch (Exception exception) {
          LOGGER.info("destroy has some error.", exception);
        }
        status = SCBStatus.FAILED;
        throw new IllegalStateException("ServiceComb init failed.", e);
      }
    }

    return this;
  }

  private void printServiceInfo() {
    StringBuilder serviceInfo = new StringBuilder();
    serviceInfo.append("Service information is shown below:\n");
    for (BootUpInformationCollector bootUpInformationCollector : bootUpInformationCollectors) {
      String info = bootUpInformationCollector.collect(this);
      if (StringUtils.isEmpty(info)) {
        continue;
      }

      serviceInfo.append(info);
      if (!info.endsWith("\n")) {
        serviceInfo.append('\n');
      }
    }
    LOGGER.info(serviceInfo.toString());
  }

  private void doRun() throws Exception {
    status = SCBStatus.STARTING;

    triggerEvent(EventType.BEFORE_FILTER);
    filterChainsManager.init();
    triggerEvent(EventType.AFTER_FILTER);

    createProducerMicroserviceMeta();

    triggerEvent(EventType.BEFORE_PRODUCER_PROVIDER);
    producerProviderManager.init();
    triggerEvent(EventType.AFTER_PRODUCER_PROVIDER);

    triggerEvent(EventType.BEFORE_CONSUMER_PROVIDER);
    consumerProviderManager.init();
    triggerEvent(EventType.AFTER_CONSUMER_PROVIDER);

    triggerEvent(EventType.BEFORE_TRANSPORT);
    transportManager.init(this);
    triggerEvent(EventType.AFTER_TRANSPORT);

    triggerEvent(EventType.BEFORE_REGISTRY);
    registrationManager.run();
    discoveryManager.run();
    // ensure can invoke services in AFTER_REGISTRY
    registrationManager.updateMicroserviceInstanceStatus(MicroserviceInstanceStatus.UP);
    status = SCBStatus.UP;
    triggerEvent(EventType.AFTER_REGISTRY);

    // Keep this message for tests cases work.
    LOGGER.warn("ServiceComb is ready.");
  }

  private void createProducerMicroserviceMeta() {
    String microserviceName = BootStrapProperties.readServiceName(environment);
    producerMicroserviceMeta = new MicroserviceMeta(this,
        BootStrapProperties.readApplication(environment), microserviceName, false);
    producerMicroserviceMeta.setProviderFilterChain(filterChainsManager.findProducerChain(
        BootStrapProperties.readApplication(environment), microserviceName));
    producerMicroserviceMeta.setMicroserviceVersionsMeta(new MicroserviceVersionsMeta(this));
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
    //Step 0: turn down the status of this instance in service center,
    // so that the consumers can remove this instance record in advance
    turnDownInstanceStatus();
    blockShutDownOperationForConsumerRefresh();

    //Step 1: notify all component stop invoke via BEFORE_CLOSE Event
    safeTriggerEvent(EventType.BEFORE_CLOSE);

    //Step 2: forbid create new consumer invocation
    status = SCBStatus.STOPPING;

    //Step 3: Unregister microservice instance from Service Center and close vertx
    // Forbidden other consumers find me
    registrationManager.destroy();
    discoveryManager.destroy();

    //Step 4: wait all invocation finished
    try {
      validAllInvocationFinished();
    } catch (InterruptedException e) {
      LOGGER.error("wait all invocation finished interrupted", e);
    }

    //Step 5: destroy config source
    // only be null for some test cases
    if (priorityPropertyManager != null) {
      priorityPropertyManager.close();
    }

    //Step 6: Stop vertx to prevent blocking exit
    // delete the following one line when every refactor is done.
    VertxUtils.blockCloseVertxByName("transport");

    HttpClients.destroy();

    //Step 7: notify all component do clean works via AFTER_CLOSE Event
    safeTriggerEvent(EventType.AFTER_CLOSE);
  }

  private void turnDownInstanceStatus() {
    try {
      registrationManager.updateMicroserviceInstanceStatus(MicroserviceInstanceStatus.DOWN);
    } catch (Throwable e) {
      LOGGER.warn("turn down instance status fail: {}", e.getMessage());
    }
  }

  private void blockShutDownOperationForConsumerRefresh() {
    try {
      long turnDownWaitSeconds = environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
          long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
      if (turnDownWaitSeconds <= 0) {
        return;
      }
      Thread.sleep(TimeUnit.SECONDS.toMillis(turnDownWaitSeconds));
    } catch (InterruptedException e) {
      LOGGER.warn("failed to block the shutdown procedure", e);
    }
  }

  private void validAllInvocationFinished() throws InterruptedException {
    long start = System.currentTimeMillis();
    while (true) {
      long remaining = invocationStartedCounter.get() - invocationFinishedCounter.get();
      if (remaining <= 0) {
        return;
      }

      if (System.currentTimeMillis() - start > TimeUnit.SECONDS.toMillis(30)) {
        LOGGER.error("wait for all requests timeout, abandon waiting, remaining requests: {}.", remaining);
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
      throw new InvocationException(Status.SERVICE_UNAVAILABLE, new CommonExceptionData(message));
    }
  }

  public CompletableFuture<MicroserviceReferenceConfig> getOrCreateReferenceConfigAsync(
      String microserviceName) {
    return referenceConfigManager.getOrCreateReferenceConfigAsync(this, microserviceName);
  }

  public MicroserviceReferenceConfig getOrCreateReferenceConfig(
      String microserviceName) {
    ensureStatusUp();
    return referenceConfigManager.getOrCreateReferenceConfig(this, microserviceName);
  }

  public MicroserviceMeta getProducerMicroserviceMeta() {
    return producerMicroserviceMeta;
  }

  public void setProducerMicroserviceMeta(MicroserviceMeta producerMicroserviceMeta) {
    this.producerMicroserviceMeta = producerMicroserviceMeta;
  }


  public static class CreateMicroserviceMetaEvent {
    private final MicroserviceMeta microserviceMeta;

    public CreateMicroserviceMetaEvent(MicroserviceMeta microserviceMeta) {
      this.microserviceMeta = microserviceMeta;
    }

    public MicroserviceMeta getMicroserviceMeta() {
      return this.microserviceMeta;
    }
  }
}
