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
import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.bootup.BootUpInformationCollector;
import org.apache.servicecomb.core.definition.ConsumerMicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.CoreMetaUtils;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.ServiceRegistryListener;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.handler.ConsumerHandlerManager;
import org.apache.servicecomb.core.handler.HandlerConfigUtils;
import org.apache.servicecomb.core.handler.ProducerHandlerManager;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.serviceregistry.DiscoveryManager;
import org.apache.servicecomb.serviceregistry.RegistrationManager;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersions;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceNameParser;
import org.apache.servicecomb.serviceregistry.event.MicroserviceInstanceRegisteredEvent;
import org.apache.servicecomb.serviceregistry.swagger.SwaggerLoader;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
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

  static final String CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC = "servicecomb.boot.turnDown.waitInSeconds";

  static final long DEFAULT_TURN_DOWN_STATUS_WAIT_SEC = 0;

  private static final Object initializationLock = new Object();

  private volatile static SCBEngine INSTANCE;

  private ConsumerHandlerManager consumerHandlerManager = new ConsumerHandlerManager();

  private ProducerHandlerManager producerHandlerManager = new ProducerHandlerManager();

  private ProducerProviderManager producerProviderManager;

  private ConsumerProviderManager consumerProviderManager = new ConsumerProviderManager();

  private MicroserviceMeta producerMicroserviceMeta;

  private TransportManager transportManager = new TransportManager();

  private List<BootListener> bootListeners = new ArrayList<>(
      SPIServiceUtils.getOrLoadSortedService(BootListener.class));

  private final AtomicLong invocationStartedCounter = new AtomicLong();

  private final AtomicLong invocationFinishedCounter = new AtomicLong();

  private volatile SCBStatus status = SCBStatus.DOWN;

  private EventBus eventBus;

  private ExecutorManager executorManager = new ExecutorManager();

  private PriorityPropertyManager priorityPropertyManager = new PriorityPropertyManager();

  protected List<BootUpInformationCollector> bootUpInformationCollectors = SPIServiceUtils
      .getSortedService(BootUpInformationCollector.class);

  private ServiceRegistryListener serviceRegistryListener;

  private SwaggerEnvironment swaggerEnvironment = new SwaggerEnvironment();

  private VendorExtensions vendorExtensions = new VendorExtensions();

  private Thread shutdownHook;

  protected SCBEngine() {
    eventBus = EventManager.getEventBus();

    eventBus.register(this);

    INSTANCE = this;

    producerProviderManager = new ProducerProviderManager(this);
    serviceRegistryListener = new ServiceRegistryListener(this);
  }

  public VendorExtensions getVendorExtensions() {
    return vendorExtensions;
  }

  public String getAppId() {
    return RegistrationManager.INSTANCE.getAppId();
  }

  public void setStatus(SCBStatus status) {
    this.status = status;
  }

  public SCBStatus getStatus() {
    return status;
  }

  public static SCBEngine getInstance() {
    if (null == INSTANCE) {
      synchronized (initializationLock) {
        if (null == INSTANCE) {
          new SCBEngine();
        }
      }
    }
    return INSTANCE;
  }

  public SwaggerLoader getSwaggerLoader() {
    return RegistrationManager.INSTANCE.getSwaggerLoader();
  }

  public ConsumerHandlerManager getConsumerHandlerManager() {
    return consumerHandlerManager;
  }

  public ProducerHandlerManager getProducerHandlerManager() {
    return producerHandlerManager;
  }

  public PriorityPropertyManager getPriorityPropertyManager() {
    return priorityPropertyManager;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  public ExecutorManager getExecutorManager() {
    return executorManager;
  }

  public void setExecutorManager(ExecutorManager executorManager) {
    this.executorManager = executorManager;
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

  public SCBEngine setTransportManager(TransportManager transportManager) {
    this.transportManager = transportManager;
    return this;
  }

  public SwaggerEnvironment getSwaggerEnvironment() {
    return swaggerEnvironment;
  }

  public Collection<BootListener> getBootListeners() {
    return bootListeners;
  }

  public void addBootListeners(Collection<BootListener> bootListeners) {
    this.bootListeners.addAll(bootListeners);
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

  /**
   * <p>As the process of instance registry is asynchronous, the {@code AFTER_REGISTRY}
   * event should not be sent immediately.
   * When the instance registry succeeds, {@link MicroserviceInstanceRegisteredEvent} will be posted in {@link EventManager},
   * register a subscriber to watch this event and send {@code AFTER_REGISTRY}.</p>
   *
   * <p>This method should be called before registry initialization to avoid that the registry process is too quick
   * that the event is not watched by this subscriber.</p>
   *
   * <p>Check if {@code InstanceId} is null to judge whether the instance registry has succeeded.</p>
   */
  private void triggerAfterRegistryEvent() {
    eventBus.register(new AfterRegistryEventHanlder(this));
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

  public synchronized SCBEngine run() {
    if (SCBStatus.DOWN.equals(status)) {
      try {
        doRun();
        waitStatusUp();
      } catch (TimeoutException e) {
        LOGGER.warn("{}", e.getMessage());
      } catch (Throwable e) {
        LOGGER.error("Failed to start ServiceComb due to errors and close", e);
        try {
          destroy();
        } catch (Exception exception) {
          LOGGER.info("destroy has some error.", exception);
        }
        status = SCBStatus.FAILED;
        throw new IllegalStateException("ServiceComb init failed.", e);
      } finally {
        printServiceInfo();
      }
    }

    return this;
  }

  private void printServiceInfo() {
    StringBuilder serviceInfo = new StringBuilder();
    serviceInfo.append("Service information is shown below:\n");
    for (BootUpInformationCollector bootUpInformationCollector : bootUpInformationCollectors) {
      serviceInfo.append(bootUpInformationCollector.collect()).append('\n');
    }
    LOGGER.info(serviceInfo.toString());
  }

  private void doRun() throws Exception {
    status = SCBStatus.STARTING;

    bootListeners.sort(Comparator.comparingInt(BootListener::getOrder));

    triggerEvent(EventType.BEFORE_HANDLER);
    HandlerConfigUtils.init(consumerHandlerManager, producerHandlerManager);
    triggerEvent(EventType.AFTER_HANDLER);

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

    triggerAfterRegistryEvent();

    RegistrationManager.INSTANCE.run();
    DiscoveryManager.INSTANCE.run();

    shutdownHook = new Thread(this::destroyForShutdownHook);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

  private void createProducerMicroserviceMeta() {
    String microserviceName = RegistrationManager.INSTANCE.getMicroservice().getServiceName();
    List<Handler> consumerHandlerChain = consumerHandlerManager.getOrCreate(microserviceName);
    List<Handler> producerHandlerChain = producerHandlerManager.getOrCreate(microserviceName);

    producerMicroserviceMeta = new MicroserviceMeta(this, microserviceName, consumerHandlerChain, producerHandlerChain);
    producerMicroserviceMeta.setMicroserviceVersionsMeta(new MicroserviceVersionsMeta(this, microserviceName));
  }

  public void destroyForShutdownHook() {
    shutdownHook = null;
    destroy();
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
    if (shutdownHook != null) {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

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
    RegistrationManager.INSTANCE.destroy();
    DiscoveryManager.INSTANCE.destroy();

    serviceRegistryListener.destroy();

    //Step 4: wait all invocation finished
    try {
      validAllInvocationFinished();
    } catch (InterruptedException e) {
      LOGGER.error("wait all invocation finished interrupted", e);
    }

    //Step 5: destroy config center source
    ConfigUtil.destroyConfigCenterConfigurationSource();
    priorityPropertyManager.close();

    //Step 6: Stop vertx to prevent blocking exit
    // delete the following one line when every refactor is done.
    VertxUtils.blockCloseVertxByName("transport");

    HttpClients.destroy();

    //Step 7: notify all component do clean works via AFTER_CLOSE Event
    safeTriggerEvent(EventType.AFTER_CLOSE);
  }

  private void turnDownInstanceStatus() {
    RegistrationManager.INSTANCE.updateMicroserviceInstanceStatus(MicroserviceInstanceStatus.DOWN);
  }

  private void blockShutDownOperationForConsumerRefresh() {
    try {
      long turnDownWaitSeconds = DynamicPropertyFactory.getInstance()
          .getLongProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)
          .get();
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
      if (remaining == 0) {
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
      LOGGER.warn(message);
      throw new InvocationException(Status.SERVICE_UNAVAILABLE, message);
    }
  }

  /**
   * for normal consumers
   * @param microserviceName shortName, or appId:shortName when invoke cross app
   * @return
   */
  public MicroserviceReferenceConfig createMicroserviceReferenceConfig(String microserviceName) {
    return createMicroserviceReferenceConfig(microserviceName, null);
  }

  /**
   * for edge, versionRule maybe controlled by url rule
   * @param microserviceName hortName, or appId:shortName when invoke cross app
   * @param versionRule if is empty, then use configuration value
   * @return
   */
  public MicroserviceReferenceConfig createMicroserviceReferenceConfig(String microserviceName, String versionRule) {
    MicroserviceVersions microserviceVersions = DiscoveryManager.INSTANCE
        .getOrCreateMicroserviceVersions(parseAppId(microserviceName), microserviceName);
    ConsumerMicroserviceVersionsMeta microserviceVersionsMeta = CoreMetaUtils
        .getMicroserviceVersionsMeta(microserviceVersions);

    return new MicroserviceReferenceConfig(microserviceVersionsMeta, versionRule);
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

  public String parseAppId(String microserviceName) {
    return parseMicroserviceName(microserviceName).getAppId();
  }

  public MicroserviceNameParser parseMicroserviceName(String microserviceName) {
    return new MicroserviceNameParser(getAppId(), microserviceName);
  }

  public static class AfterRegistryEventHanlder {
    private SCBEngine engine;

    public AfterRegistryEventHanlder(SCBEngine engine) {
      this.engine = engine;
    }

    @Subscribe
    @EnableExceptionPropagation
    public void afterRegistryInstance(MicroserviceInstanceRegisteredEvent microserviceInstanceRegisteredEvent) {
      LOGGER.info("receive MicroserviceInstanceRegisteredEvent event, check instance Id...");

      if (!StringUtils.isEmpty(RegistrationManager.INSTANCE.getMicroserviceInstance().getInstanceId())) {
        LOGGER.info("instance registry succeeds for the first time, will send AFTER_REGISTRY event.");
        engine.setStatus(SCBStatus.UP);
        engine.triggerEvent(EventType.AFTER_REGISTRY);
        EventManager.unregister(this);
        LOGGER.warn("ServiceComb is ready.");
      }
    }
  }
}
